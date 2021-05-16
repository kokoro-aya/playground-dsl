
fun Array<Int>.swap(i: Int, j: Int) {
    val tmp = this[i]; this[i] = this[j]; this[j] = tmp
}

fun partition(a: Array<Int>, left: Int, right: Int): Int {
    val pivot = left
    var index = pivot + 1
    for (i in index .. right) {
    if (a[i] < a[pivot]) {
        a.swap(i, index)
        index += 1
    }
}
    a.swap(pivot, index - 1)
    return index - 1
}

fun quickSort(a: Array<Int>, left: Int, right: Int): Array<Int> {
    if (left < right) {
        val index = partition(a, left, right)
        quickSort(a, left, index - 1)
        quickSort(a, index + 1, right)
    }
    return a
}

val a = arrayOf(9,4,6,13,2,19,7,5,7,1,22,8,6)
quickSort(a, 0, a.size - 1)
for (x in a) {
    print("$x")
}
