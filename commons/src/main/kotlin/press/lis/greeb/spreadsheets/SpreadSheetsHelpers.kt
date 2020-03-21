package press.lis.greeb.spreadsheets

/**
 * @author Aleksandr Eliseev
 */
object SpreadSheetsHelpers {
    fun columnNumberToA1Notation(columnNumber: Int): String {
        var columnNumberInternal: Int = columnNumber
        val sb = StringBuilder()
        val numberOfLetters = 26

        while (true) {
            sb.append('A' + columnNumberInternal % numberOfLetters)

            if (columnNumberInternal < numberOfLetters) {
                break
            }

            columnNumberInternal /= numberOfLetters
            columnNumberInternal--
        }

        return sb.reverse().toString()
    }
}