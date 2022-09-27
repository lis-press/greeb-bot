package press.lis.greeb.spreadsheets

/**
 * @author Aleksandr Eliseev
 */
object SpreadSheetsHelpers {
    private const val NUMBER_OF_LETTERS = 26

    /**
     * For example:
     * 0 -> A
     * 26 -> AA
     */
    fun columnNumberToA1Notation(columnNumber: Int): String {
        var columnNumberInternal: Int = columnNumber
        val sb = StringBuilder()

        while (true) {
            sb.append('A' + columnNumberInternal % NUMBER_OF_LETTERS)

            if (columnNumberInternal < NUMBER_OF_LETTERS) {
                break
            }

            columnNumberInternal /= NUMBER_OF_LETTERS
            columnNumberInternal--
        }

        return sb.reverse().toString()
    }

    /**
     * For example:
     * A -> 0
     * AA -> 26
     */
    fun columnNameToNumber(columnName: String): Int {
        var result = 0

        for (ch: Char in columnName) {
            result *= NUMBER_OF_LETTERS
            result += ch - 'A' + 1
        }

        return result - 1
    }
}