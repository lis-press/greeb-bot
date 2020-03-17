package press.lis.greeb.spreadsheets

import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Aleksandr Eliseev
 */

class SpreadSheetsHelpersTest {
    @Test fun testColumnNumberToA1Notation() : Unit {
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(0), "A")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(1), "B")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(25), "Z")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(26), "AA")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(27), "AB")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(701), "ZZ")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(702), "AAA")
        assertEquals(SpreadSheetsHelpers.columnNumberToA1Notation(703), "AAB")
    }
}