package org.ironica.playground

import org.antlr.v4.runtime.tree.*

import org.ironica.playground.SpecialRetVal.*
import org.ironica.playground.Type.*
import org.ironica.playground.Variability.*
import playgroundGrammarVisitor
import kotlin.math.pow

enum class SpecialRetVal {
    Interr, Loop, Statements, Declaration, Branch, Empty, ReDef, NotDef
}

enum class Type {
    INT, DOUBLE, CHARACTER, STRING, BOOL, VOID, CALL
}

enum class Variability {
    VAR, LET
}

sealed class TypedLiteral
data class IntLiteral(val variability: Variability, val content: Int): TypedLiteral()
data class DoubleLiteral(val variability: Variability, val content: Double): TypedLiteral()
data class BooleanLiteral(val variability: Variability, val content: Boolean): TypedLiteral()
data class CharacterLiteral(val variability: Variability, val content: Char): TypedLiteral()
data class StringLiteral(val variability: Variability, val content: String): TypedLiteral()

// TODO: object NullLiteral: TypedLiteral()

data class ReturnedLiteral(val content: Any): Throwable()

typealias TypedParam = Pair<String, Type>
typealias TypedArgum = Pair<Any, Type>

data class FunctionHead(val name: String, val params: List<String>, val types: List<Type>, val ret: Type) {
    fun pseudoEquals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FunctionHead

        if (name != other.name) return false
        if (types != other.types) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + types.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FunctionHead

        if (name != other.name) return false
        if (params != other.params) return false
        if (types != other.types) return false
        if (ret != other.ret) return false

        return true
    }
}

class PlaygroundVisitor(private val manager: PlaygroundManager): playgroundGrammarVisitor<Any> {

    private val variableTable = mutableMapOf<String, TypedLiteral>()

    private val functionTable = mutableMapOf<FunctionHead, ParseTree>()

    private var _break = false
    private var _continue = false

    private var _return = false

    private val internalVariables = mutableMapOf<String, TypedLiteral>()

    override fun visitIsNestedCondition(ctx: playgroundGrammarParser.IsNestedConditionContext?): Any {
        val left: Boolean = visit(ctx?.expression(0)) as Boolean
        val right = visit(ctx?.expression(1)) as Boolean
        return if (ctx?.op?.type == playgroundGrammarParser.AND) left && right else left || right
    }

    override fun visitStatements(ctx: playgroundGrammarParser.StatementsContext?): Any {
        try {
            var ret: Any = Statements
            for (child in ctx?.children!!) {
                if (_break || _continue)
                    break
                ret = visit(child)
            }
            return ret
        } catch (e: Throwable) {
            if (e is ReturnedLiteral)
                throw e
            else
                throw Exception("Something goes wrong while visiting statements:\n    ${e.message}")
        }
    }

    override fun visitLoop_statement(ctx: playgroundGrammarParser.Loop_statementContext?): Any {
        return visit(ctx?.children?.get(0))
    }

    override fun visitFor_in_statement(ctx: playgroundGrammarParser.For_in_statementContext?): Any {
        val range = visit(ctx?.expression()) as IntRange
        val pattern = visit(ctx?.pattern()) as String
        // println(range)
        for (x in range) {
            if (pattern != "_") {
                variableTable[pattern] = IntLiteral(VAR, x)
                // TODO this might cause problem if the pattern shallows another variable in the outer scope?
            }
            if (_break) {
                _break = false
                break
            }
            if (_continue) {
                _continue = false
                continue
            }
            visit(ctx?.code_block())
        }
        variableTable.remove(pattern)
        return Loop
    }

    override fun visitBranch_statement(ctx: playgroundGrammarParser.Branch_statementContext?): Any {
        return visit(ctx?.if_statement())
    }

    override fun visitElse_clause(ctx: playgroundGrammarParser.Else_clauseContext?): Any {
        return visit(ctx?.children?.get(1))
    }

    override fun visitErrorNode(node: ErrorNode?): Any {
        throw Exception("Return from ErrorNode")
    }

    override fun visitRepeat_while_statement(ctx: playgroundGrammarParser.Repeat_while_statementContext?): Any {
         do {
            if (_break) {
                _break = false
                break
            }
            if (_continue) {
                _continue = false
                continue
            }
            visit(ctx?.code_block())
        } while (visit(ctx?.expression()) == true)
        return Loop
    }

    override fun visitControl_transfer_statement(ctx: playgroundGrammarParser.Control_transfer_statementContext?): Any {
        return visit(ctx?.children?.get(0))
    }

    override fun visitDeclaration(ctx: playgroundGrammarParser.DeclarationContext?): Any {
        return visit(ctx?.children?.get(0))
    }

    override fun visitFunction_signature(ctx: playgroundGrammarParser.Function_signatureContext?): Any {
        val paramClause = visit(ctx?.parameter_clause()) as List<TypedParam>
        val resultType = if (ctx?.childCount == 1) VOID else visit(ctx?.function_result_type()) as Type
        return Pair(paramClause, resultType)
    }

    override fun visit(tree: ParseTree?): Any {
        return tree?.accept(this) ?: throw Exception("Encountered error while visiting AST")
    }

    override fun visitTop_level(ctx: playgroundGrammarParser.Top_levelContext?): Any {
        return visit(ctx?.statements())
    }

    override fun visitStatement(ctx: playgroundGrammarParser.StatementContext?): Any {
        return visit(ctx?.children?.get(0))
    }

    override fun visitWhile_statement(ctx: playgroundGrammarParser.While_statementContext?): Any {
        while (visit(ctx?.expression()) == true) {
            if (_break) {
                _break = false
                break
            }
            if (_continue) {
                _continue = false
                break
            }
            visit(ctx?.code_block())
        }
        return Loop
    }

    override fun visitIf_statement(ctx: playgroundGrammarParser.If_statementContext?): Any {
        if (visit(ctx?.expression()) == true) {
            visit(ctx?.code_block())
        } else {
            if (ctx?.else_clause() != null) {
                visit(ctx.else_clause())
            }
        }
        return Branch
    }

    override fun visitBreak_statement(ctx: playgroundGrammarParser.Break_statementContext?): Any {
        _break = true
        return Interr
    }

    override fun visitContinue_statement(ctx: playgroundGrammarParser.Continue_statementContext?): Any {
        _continue = true
        return Interr
    }

    override fun visitCode_block(ctx: playgroundGrammarParser.Code_blockContext?): Any {
        return visit(ctx?.statements())
    }

    private fun declareOrAssignConstantOrVariable(left: String, right: Any, constant: Boolean, inFunc: Boolean): Boolean {
        if (inFunc) {
            if (constant) {
                if (!internalVariables.containsKey(left)) {
                    internalVariables[left] = when (right) {
                        is Int -> IntLiteral(LET, right)
                        is Double -> DoubleLiteral(LET, right)
                        is Boolean -> BooleanLiteral(LET, right)
                        is Char -> CharacterLiteral(LET, right)
                        is String -> StringLiteral(LET, right)
                        else -> throw Exception("Cannot recognize type while declaring or assigning a variable")
                    }
                    return true
                }
            } else {
                when (right) {
                    is Int -> if (!internalVariables.containsKey(left) || (internalVariables[left] as IntLiteral).variability == VAR) {
                        internalVariables[left] = IntLiteral(VAR, right)
                        return true
                    }
                    is Double -> if (!internalVariables.containsKey(left) || (internalVariables[left] as DoubleLiteral).variability == VAR) {
                        internalVariables[left] = DoubleLiteral(VAR, right)
                        return true
                    }
                    is Boolean -> if (!internalVariables.containsKey(left) || (internalVariables[left] as BooleanLiteral).variability == VAR) {
                        internalVariables[left] = BooleanLiteral(VAR, right)
                        return true
                    }
                    is Char -> if (!internalVariables.containsKey(left) || (internalVariables[left] as CharacterLiteral).variability == VAR) {
                        internalVariables[left] = CharacterLiteral(VAR, right)
                        return true
                    }
                    is String -> if (!internalVariables.containsKey(left) || (internalVariables[left] as StringLiteral).variability == VAR) {
                        internalVariables[left] = StringLiteral(VAR, right)
                        return true
                    }
                    else -> throw Exception("Cannot recognize type while declaring or assigning a variable")
                }
            }
        } else {
            if (constant) {
                if (!variableTable.containsKey(left)) {
                    variableTable[left] = when (right) {
                        is Int -> IntLiteral(LET, right)
                        is Double -> DoubleLiteral(LET, right)
                        is Boolean -> BooleanLiteral(LET, right)
                        is Char -> CharacterLiteral(LET, right)
                        is String -> StringLiteral(LET, right)
                        else -> throw Exception("Cannot recognize type while declaring or assigning a variable")
                    }
                    return true
                }
            } else {
                when (right) {
                    is Int -> if (!variableTable.containsKey(left) || (variableTable[left] as IntLiteral).variability == VAR) {
                        variableTable[left] = IntLiteral(VAR, right)
                        return true
                    }
                    is Double -> if (!variableTable.containsKey(left) || (variableTable[left] as DoubleLiteral).variability == VAR) {
                        variableTable[left] = DoubleLiteral(VAR, right)
                        return true
                    }
                    is Boolean -> if (!variableTable.containsKey(left) || (variableTable[left] as BooleanLiteral).variability == VAR) {
                        variableTable[left] = BooleanLiteral(VAR, right)
                        return true
                    }
                    is Char -> if (!variableTable.containsKey(left) || (variableTable[left] as CharacterLiteral).variability == VAR) {
                        variableTable[left] = CharacterLiteral(VAR, right)
                        return true
                    }
                    is String -> if (!variableTable.containsKey(left) || (variableTable[left] as StringLiteral).variability == VAR) {
                        variableTable[left] = StringLiteral(VAR, right)
                        return true
                    }
                    else -> throw Exception("Cannot recognize type while declaring or assigning a variable")
                }
            }
        }
        return false
    }

    override fun visitConstant_declaration(ctx: playgroundGrammarParser.Constant_declarationContext?): Any {
        val left = visit(ctx?.pattern()) as String
        if (!(left[0].isLetter() || left[0] == '_')) throw Exception("Illegal variable name")
        val right = visit(ctx?.expression())
        // println("right: $right")
        if (ctx?.parent?.parent?.parent?.parent?.parent is playgroundGrammarParser.Function_bodyContext) {
            if (declareOrAssignConstantOrVariable(left, right, constant = true, inFunc = true)) return Declaration
        } else {
            if (declareOrAssignConstantOrVariable(left, right, constant = true, inFunc = false)) return Declaration
        }
        throw Exception("Encountered error while declaring constant")
    }

    override fun visitVariable_declaration(ctx: playgroundGrammarParser.Variable_declarationContext?): Any {
        val left = visit(ctx?.pattern()) as String
        if (!(left[0].isLetter() || left[0] == '_')) throw Exception("Illegal variable name")
        val right = visit(ctx?.expression())
        if (ctx?.parent?.parent?.parent?.parent?.parent is playgroundGrammarParser.Function_bodyContext) {
            if (declareOrAssignConstantOrVariable(left, right, constant = false, inFunc = true)) return Declaration
        } else {
            if (declareOrAssignConstantOrVariable(left, right, constant = false, inFunc = false)) return Declaration
        }
        throw Exception("Encountered error while declaring variable")
    }

    override fun visitTerminal(node: TerminalNode?): Any {
        var ret: Any? = node?.text?.toIntOrNull()
        if (ret != null) return (ret as Int)
        else {
            ret = node?.text?.toDoubleOrNull()
            if (ret != null) return (ret as Double)
            else {
                if (node?.text.toString() == "true") return true
                if (node?.text.toString() == "false") return false
                if (node?.text != null) {
                    return if (node.text?.length == 1)
                        node.text[0]
                    else
                        node.text
                }
                throw Exception("Encountered error while visiting terminal node")
            }
        }
    }

    override fun visitFunction_body(ctx: playgroundGrammarParser.Function_bodyContext?): Any {
        return visit(ctx?.code_block())
    }

    override fun visitChildren(node: RuleNode?): Any {
        TODO("Not yet implemented")
    }

    override fun visitFunction_declaration(ctx: playgroundGrammarParser.Function_declarationContext?): Any {
        try {
            val functionName = visit(ctx?.function_name()) as String
            val functionSignature = visit(ctx?.function_signature()) as Pair<List<TypedParam>, Type>
            val functionHead = FunctionHead(functionName, functionSignature.first.map { it.first }, functionSignature.first.map { it.second }, functionSignature.second)
            for (key in functionTable.keys) {
                if (functionHead.pseudoEquals(key))
                    return ReDef
            }
            functionTable[functionHead] = ctx?.function_body()!!
            return Declaration
        } catch (e: Exception) {
            throw Exception("Encountered error within function declaration: \n    ${e.message}")
        }
    }

    override fun visitPattern(ctx: playgroundGrammarParser.PatternContext?): Any {
        return visit(ctx?.children?.get(0))
    }

    override fun visitWildcard_pattern(ctx: playgroundGrammarParser.Wildcard_patternContext?): Any {
        return "_"
    }

    override fun visitIdentifier_pattern(ctx: playgroundGrammarParser.Identifier_patternContext?): Any {
        return ctx?.IDENTIFIER()?.text ?: throw Exception("Null identifier encountered")
    }

    override fun visitIsNegativeCondition(ctx: playgroundGrammarParser.IsNegativeConditionContext?): Any {
        return !(visit(ctx?.children?.get(1)) as Boolean)
    }

    override fun visitBoolean_literal(ctx: playgroundGrammarParser.Boolean_literalContext?): Any {
        return when (ctx?.text) {
            "true" -> true
            "false"-> false
            else -> throw Exception("Cannot parse literal to boolean type")
        }
    }

    override fun visitFunction_name(ctx: playgroundGrammarParser.Function_nameContext?): Any {
        return ctx?.IDENTIFIER()?.text ?: throw Exception("Function name cannot be null")
    }

    override fun visitFunction_result_type(ctx: playgroundGrammarParser.Function_result_typeContext?): Any {
        return when (val text = ctx?.type()?.text) {
            "Int" -> INT
            "Bool" -> BOOL
            "Double" -> DOUBLE
            "Character" -> CHARACTER
            "String" -> STRING
            "Void", "" -> VOID
            else -> throw Exception("Unsupported function return type")
        }
    }

    override fun visitParameter_clause(ctx: playgroundGrammarParser.Parameter_clauseContext?): Any {
        if (ctx?.children?.size == 1)
            return listOf<TypedParam>()
        return visit(ctx?.parameter_list())
    }

    override fun visitParameter_list(ctx: playgroundGrammarParser.Parameter_listContext?): Any {
        return ctx?.parameter()?.map { (visit(it) as TypedParam) }!!
    }

    override fun visitParameter(ctx: playgroundGrammarParser.ParameterContext?): Any {
        val param = visit(ctx?.param_name()) as String
        val type = visit(ctx?.type_annotation()) as Type
        return Pair(param, type) as TypedParam
    }

    override fun visitParam_name(ctx: playgroundGrammarParser.Param_nameContext?): Any {
        return ctx?.IDENTIFIER()?.text ?: throw Exception("Function parameter name cannot be null")
    }

    override fun visitType_annotation(ctx: playgroundGrammarParser.Type_annotationContext?): Any {
        return visit(ctx?.type())
    }

    override fun visitType(ctx: playgroundGrammarParser.TypeContext?): Any {
        return when (ctx?.text) {
            "Int" -> INT
            "Bool" -> BOOL
            "Double" -> DOUBLE
            "Character" -> CHARACTER
            "String" -> STRING
            "Void", "" -> VOID
            else -> throw Exception("Unsupported data type")
        }
    }

    override fun visitFunction_call(ctx: playgroundGrammarParser.Function_callContext?): Any {
        return visit(ctx?.function_call_expression())
    }

    // TODO recheck the function call, especially when applying the parameters
    override fun visitFunction_call_expression(ctx: playgroundGrammarParser.Function_call_expressionContext?): Any {
        try {
            val functionName = visit(ctx?.function_name()) as String
            val funcArgument =
                if (ctx?.childCount == 2) listOf() else visit(ctx?.call_argument_clause()) as List<TypedArgum>
            val functionHead = FunctionHead(functionName, listOf(), funcArgument.map { it.second }, CALL)
            if (functionHead.name == "moveForward" && functionHead.types.isEmpty()) {
                return manager.moveForward()
            } else if (functionHead.name == "turnLeft" && functionHead.types.isEmpty()) {
                return manager.turnLeft()
            } else if (functionHead.name == "toggleSwitch" && functionHead.types.isEmpty()) {
                return manager.toggleSwitch()
            } else if (functionHead.name == "collectGem" && functionHead.types.isEmpty()) {
                return manager.collectGem()
            } else if (functionHead.name == "print") {
                manager.print(funcArgument.map{ if (it.first is String && (it.first as String)[0] == '"') (it.first as String).substring(1, (it.first as String).length - 1) else it.first.toString() })
                return Empty
            } else {
                for (key in functionTable.keys) {
                    if (functionHead.pseudoEquals(key)) {
                        val realHead = key
                        if (realHead.params.isEmpty()) {
                            try {
                                return visit(functionTable[realHead])
                            } catch (e: Throwable) {
                                if (e is ReturnedLiteral)
                                    return e.content
                                else throw e
                            }
                        } else {
                            val paramContents = funcArgument.map { it.first }.map {
                                when (val content = it) {
                                    is Int -> IntLiteral(VAR, content)
                                    is Double -> DoubleLiteral(VAR, content)
                                    is Boolean -> BooleanLiteral(VAR, content)
                                    is Char -> CharacterLiteral(VAR, content)
                                    is String -> StringLiteral(VAR, content)
                                    else -> throw Exception("Unsupported argument type")
                                }
                            }
                            val paramNames = realHead.params
                            for (idx in paramNames.indices) {
                                internalVariables[paramNames[idx]] = paramContents[idx]
                            }
                            try {
                                val ret = visit(functionTable[realHead])
                                for (name in paramNames) {
                                    internalVariables.remove(name)
                                }
                                return ret
                            } catch (e: Throwable) {
                                if (e is ReturnedLiteral) {
                                    for (name in paramNames)
                                        internalVariables.remove(name)
                                    return e.content
                                }
                                else throw e
                            }
                        }
                    }
                }
                return NotDef
            }
        } catch (e: Exception) {
            throw Exception("Something went wrong while passing function call: \n    ${e.message}")
        }
    }

    override fun visitCall_argument_clause(ctx: playgroundGrammarParser.Call_argument_clauseContext?): Any {
        return ctx?.call_argument()?.map { visit(it) }!! as List<TypedArgum>
    }

    override fun visitCall_argument(ctx: playgroundGrammarParser.Call_argumentContext?): Any {
        val callval = visit(ctx?.expression())
        val type: Type = when (callval) {
            is Int -> INT
            is Double -> DOUBLE
            is Boolean -> BOOL
            is Char -> CHARACTER
            is String -> STRING
            else -> throw Exception("Unrecognized argument type")
        }
        return Pair(callval, type) as TypedArgum
    }

    override fun visitLiteral(ctx: playgroundGrammarParser.LiteralContext?): Any {
        return visit(ctx?.children?.get(0))
    }

    override fun visitNumeric_literal(ctx: playgroundGrammarParser.Numeric_literalContext?): Any {
        return when {
            ctx?.childCount == 1 -> visit(ctx.getChild(0))
            ctx?.integer_literal()?.isEmpty!! -> - (visit(ctx.getChild(1)) as Double)
            else -> - (visit(ctx.getChild(1)) as Int)
        }
    }

    override fun visitChar_sequence_literal(ctx: playgroundGrammarParser.Char_sequence_literalContext?): Any {
        return if (ctx?.getChild(0)?.text?.length == 1)
            ctx.getChild(0).text[0]
        else
            ctx?.getChild(0)?.text!!
    }

    override fun visitAssignmentExpr(ctx: playgroundGrammarParser.AssignmentExprContext?): Any {
        return visit(ctx?.assignment_expression())
    }

    override fun visitBoolComparativeExpr(ctx: playgroundGrammarParser.BoolComparativeExprContext?): Any {
        val left = visit(ctx?.expression(0))
        val right = visit(ctx?.expression(1))
        if (left is Boolean && right is Boolean) {
            return if (ctx?.op?.type == playgroundGrammarParser.EQ) left == right else left != right
        }
        if ((left is Int || left is Double) && right is Boolean) {
            return if (ctx?.op?.type == playgroundGrammarParser.EQ) (left != 0) == right else (left != 0) != right
        }
        if (left is Boolean && (right is Int || right is Double)) {
            return if (ctx?.op?.type == playgroundGrammarParser.EQ) left == (right != 0) else left != (right != 0)
        }
        if ((left is Int || left is Double) && (right is Int || right is Double)) {
            return if (ctx?.op?.type == playgroundGrammarParser.EQ) (left != 0) == (right != 0) else (left != 0) != (right != 0)
        }
        throw Exception("Boolean comparative expression on unsupported type")
    }

    override fun visitExponentExpr(ctx: playgroundGrammarParser.ExponentExprContext?): Any {
        val left = visit(ctx?.expression(0)).toString().toDouble()
        val right = visit(ctx?.expression(1)).toString().toDouble()
        return left.pow(right)
    }

    override fun visitAddSubExpr(ctx: playgroundGrammarParser.AddSubExprContext?): Any {
        var left = visit(ctx?.expression(0))
        var right = visit(ctx?.expression(1))
        if ((left is Boolean || left is Int) && (right is Boolean || right is Int)) {
            if (left is Boolean)
                left = if (left == true) 1 else 0
            if (right is Boolean)
                right = if (right == true) 1 else 0
            return when (ctx?.op?.type) {
                playgroundGrammarParser.ADD -> left as Int + right as Int
                else -> left as Int - right as Int
            }
        }
        if ((left is Boolean || left is Double) && (right is Boolean || right is Double)) {
            if (left is Boolean)
                left = if (left == true) 1 else 0
            if (right is Boolean)
                right = if (right == true) 1 else 0
            return when (ctx?.op?.type) {
                playgroundGrammarParser.ADD -> left as Double + right as Double
                else -> left as Double - right as Double
            }
        }
        throw Exception("AddSub: on unsupported type")
    }

    override fun visitAriAssignmentExpr(ctx: playgroundGrammarParser.AriAssignmentExprContext?): Any {
        val left = visit(ctx?.pattern()) as String
        if (!(left[0].isLetter() || left[0] == '_')) throw Exception("Illegal variable name")
        val right = visit(ctx?.expression())
        if (ctx?.parent?.parent?.parent?.parent is playgroundGrammarParser.Function_bodyContext) {
            if (internalVariables[left] is IntLiteral && right is Int && (internalVariables[left] as IntLiteral).variability == VAR) {
                val old = (internalVariables[left] as IntLiteral).content
                val new = when (ctx?.op?.type) {
                    playgroundGrammarParser.ADDEQ -> old + right
                    playgroundGrammarParser.SUBEQ -> old - right
                    playgroundGrammarParser.MULEQ -> old * right
                    playgroundGrammarParser.DIVEQ -> old / right
                    else -> old % right
                }
                internalVariables[left] = IntLiteral(VAR, new)
                return new
            }
            if (internalVariables[left] is DoubleLiteral && right is Double && (internalVariables[left] as DoubleLiteral).variability == VAR) {
                val old = (internalVariables[left] as DoubleLiteral).content
                val new = when (ctx?.op?.type) {
                    playgroundGrammarParser.ADDEQ -> old + right
                    playgroundGrammarParser.SUBEQ -> old - right
                    playgroundGrammarParser.MULEQ -> old * right
                    playgroundGrammarParser.DIVEQ -> old / right
                    else -> old % right
                }
                internalVariables[left] = DoubleLiteral(VAR, new)
                return new
            }
        } else {
            if (variableTable[left] is IntLiteral && right is Int && (variableTable[left] as IntLiteral).variability == VAR) {
                val old = (variableTable[left] as IntLiteral).content
                val new = when (ctx?.op?.type) {
                    playgroundGrammarParser.ADDEQ -> old + right
                    playgroundGrammarParser.SUBEQ -> old - right
                    playgroundGrammarParser.MULEQ -> old * right
                    playgroundGrammarParser.DIVEQ -> old / right
                    else -> old % right
                }
                variableTable[left] = IntLiteral(VAR, new)
                return new
            }
            if (variableTable[left] is DoubleLiteral && right is Double && (variableTable[left] as DoubleLiteral).variability == VAR) {
                val old = (variableTable[left] as DoubleLiteral).content
                val new = when (ctx?.op?.type) {
                    playgroundGrammarParser.ADDEQ -> old + right
                    playgroundGrammarParser.SUBEQ -> old - right
                    playgroundGrammarParser.MULEQ -> old * right
                    playgroundGrammarParser.DIVEQ -> old / right
                    else -> old % right
                }
                variableTable[left] = DoubleLiteral(VAR, new)
                return new
            }
        }
        throw Exception("Arithmetic assignment: unsupported type")
    }

    override fun visitMemberExpr(ctx: playgroundGrammarParser.MemberExprContext?): Any {
        TODO("Not yet implemented")
    }

    override fun visitAriComparativeExpr(ctx: playgroundGrammarParser.AriComparativeExprContext?): Any {
        var left = visit(ctx?.expression(0))
        var right = visit(ctx?.expression(1))
        if (left is Int && right is Double)
            left = left.toDouble()
        if (left is Double && right is Int)
            right = right.toDouble()
        if (left is Int && right is Int) {
            return when (ctx?.op?.type) {
                playgroundGrammarParser.GT -> left > right
                playgroundGrammarParser.LT -> left < right
                playgroundGrammarParser.GEQ -> left >= right
                else -> left <= right
            }
        }
        if (left is Double && right is Double) {
            return when (ctx?.op?.type) {
                playgroundGrammarParser.GT -> left > right
                playgroundGrammarParser.LT -> left < right
                playgroundGrammarParser.GEQ -> left >= right
                else -> left <= right
            }
        }
        throw Exception("Arithmetic comparable expression on unsupported type")
    }

    override fun visitLiteralValueExpr(ctx: playgroundGrammarParser.LiteralValueExprContext?): Any {
        return visit(ctx?.literal_expression())
    }

    override fun visitMulDivModExpr(ctx: playgroundGrammarParser.MulDivModExprContext?): Any {
        var left = visit(ctx?.expression(0))
        var right = visit(ctx?.expression(1))
        if ((left is Boolean || left is Int) && (right is Boolean || right is Int)) {
            if (left is Boolean)
                left = if (left == true) 1 else 0
            if (right is Boolean)
                right = if (right == true) 1 else 0
            return when (ctx?.op?.type) {
                playgroundGrammarParser.MUL -> left as Int * right as Int
                playgroundGrammarParser.DIV -> left as Int / right as Int
                else -> left as Int % right as Int
            }
        }
        if ((left is Boolean || left is Double) && (right is Boolean || right is Double)) {
            if (left is Boolean)
                left = if (left == true) 1 else 0
            if (right is Boolean)
                right = if (right == true) 1 else 0
            return when (ctx?.op?.type) {
                playgroundGrammarParser.MUL -> left as Double * right as Double
                playgroundGrammarParser.DIV -> left as Double / right as Double
                else -> left as Double % right as Double
            }
        }
        throw Exception("MulDivMod: on unsupported type")
    }

    override fun visitVariableExpr(ctx: playgroundGrammarParser.VariableExprContext?): Any {
        return visit(ctx?.variable_expression())
    }

    override fun visitRangeExpression(ctx: playgroundGrammarParser.RangeExpressionContext?): Any {
        val lower = visit(ctx?.expression(0)) as Int
        val upper = visit(ctx?.expression(1)) as Int
        // println("lower $lower upper $upper")
        if (ctx?.op?.type == playgroundGrammarParser.UNTIL) {
            return (lower until upper)
        }
        if (ctx?.op?.type == playgroundGrammarParser.THROUGH) {
            return (lower .. upper)
        }
        throw Exception("Something goes wrong within range expression")
    }

    override fun visitParenthesisExpr(ctx: playgroundGrammarParser.ParenthesisExprContext?): Any {
        return visit(ctx?.expression())
    }

    override fun visitAssignment_expression(ctx: playgroundGrammarParser.Assignment_expressionContext?): Any {
        val left = visit(ctx?.pattern()) as String
        if (!(left[0].isLetter() || left[0] == '_')) throw Exception("Illegal variable name")
        val right = visit(ctx?.expression())
        if (ctx?.parent?.parent?.parent?.parent?.parent is playgroundGrammarParser.Function_bodyContext) {
            if (declareOrAssignConstantOrVariable(left, right, constant = false, inFunc = true)) return internalVariables[left]!!
        } else {
            if (declareOrAssignConstantOrVariable(left, right, constant = false, inFunc = false)) return variableTable[left]!!
        }
        throw Exception("Something goes wrong within assignment expression, or maybe you assign to a constant")
    }

    override fun visitLiteral_expression(ctx: playgroundGrammarParser.Literal_expressionContext?): Any {
        return visit(ctx?.literal())
    }

    override fun visitMember_expression(ctx: playgroundGrammarParser.Member_expressionContext?): Any {
        TODO("Not yet implemented")
    }

    override fun visitVariable_expression(ctx: playgroundGrammarParser.Variable_expressionContext?): Any {
        return when (val name = visit(ctx?.IDENTIFIER()).toString()) {
            "isOnGem" -> manager.isOnGem()
            "isOnOpenedSwitch" -> manager.isOnOpenedSwitch()
            "isOnClosedSwitch" -> manager.isOnClosedSwitch()
            "isBlocked" -> manager.isBlocked()
            "isBlockedLeft" -> manager.isBlockedLeft()
            "isBlockedRight" -> manager.isBlockedRight()
            "collectedGem" -> manager.collectedGem()
            else -> {
                when {
                    internalVariables.containsKey(name) -> {
                        when (val literal = internalVariables[name]!!) {
                            is IntLiteral -> literal.content
                            is DoubleLiteral -> literal.content
                            is CharacterLiteral -> literal.content
                            is StringLiteral -> literal.content
                            is BooleanLiteral -> literal.content
                        }
                    }
                    variableTable.containsKey(name) -> {
                        when (val literal = variableTable[name]!!) {
                            is IntLiteral -> literal.content
                            is DoubleLiteral -> literal.content
                            is CharacterLiteral -> literal.content
                            is StringLiteral -> literal.content
                            is BooleanLiteral -> literal.content
                        }
                    }
                    else -> {
                        NotDef
                    }
                }
            }
        }
    }

    override fun visitReturn_statement(ctx: playgroundGrammarParser.Return_statementContext?): Any {
        val ret = ReturnedLiteral(visit(ctx?.expression()))
        throw ret
    }

    override fun visitInteger_literal(ctx: playgroundGrammarParser.Integer_literalContext?): Any {
        return "${visit(ctx?.DECIMAL_LITERAL())}".toInt()
    }

    override fun visitDouble_literal(ctx: playgroundGrammarParser.Double_literalContext?): Any {
        return "${visit(ctx?.DECIMAL_LITERAL(0))}.${visit(ctx?.DECIMAL_LITERAL(1))}".toDouble()
    }
}

/*
func foo(a: Int, b: String) -> Int {
    print(b)
    a += 1
    return a
}

let b = 3
let c = foo(b, "bar")
print(c)
 */
/*
let a = 1
func foo() {
    let a = 2
    print(a)
}
print(a)
 */