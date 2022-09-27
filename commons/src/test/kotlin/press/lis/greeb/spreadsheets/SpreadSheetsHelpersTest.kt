package press.lis.greeb.spreadsheets

import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Aleksandr Eliseev
 */

class SpreadSheetsHelpersTest {
    @Test fun testColumnNumberToA1Notation() {
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(0), "A")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(1), "B")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(25), "Z")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(26), "AA")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(27), "AB")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(701), "ZZ")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(702), "AAA")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(703), "AAB")
    }

    @Test fun testColumnNameToNumber() {
        assertEquals(SpreadSheetsHelpers.columnNameToNumber("A"), 0)
        assertEquals(SpreadSheetsHelpers.columnNameToNumber("B"), 1)
        assertEquals(SpreadSheetsHelpers.columnNameToNumber("Z"), 25)
        assertEquals(SpreadSheetsHelpers.columnNameToNumber("AA"), 26)
        assertEquals(SpreadSheetsHelpers.columnNameToNumber("AB"), 27)
        assertEquals(SpreadSheetsHelpers.columnNameToNumber("ZZ"), 701)
        assertEquals(SpreadSheetsHelpers.columnNameToNumber("AAA"), 702)
        assertEquals(SpreadSheetsHelpers.columnNameToNumber("AAB"), 703)
    }
}