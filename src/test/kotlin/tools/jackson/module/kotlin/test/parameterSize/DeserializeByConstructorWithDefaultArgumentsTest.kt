package tools.jackson.module.kotlin.test.parameterSize

import tools.jackson.module.kotlin.assertReflectEquals
import tools.jackson.module.kotlin.defaultMapper
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Up to argument size 32 there is one mask argument for the default argument,
 * 33 ~ 64 there are two, and 65 there are three, so each boundary value is tested.
 * Also, if the default argument is set, the maximum argument size that can be set in the constructor is 245,
 * so that case is tested as well.
 */
class DeserializeByConstructorWithDefaultArgumentsTest {
    data class Dst32(
        val p00: String = "0",
        val p01: String = "1",
        val p02: String = "2",
        val p03: String = "3",
        val p04: String = "4",
        val p05: String = "5",
        val p06: String = "6",
        val p07: String = "7",
        val p08: String = "8",
        val p09: String = "9",
        val p10: String = "10",
        val p11: String = "11",
        val p12: String = "12",
        val p13: String = "13",
        val p14: String = "14",
        val p15: String = "15",
        val p16: String = "16",
        val p17: String = "17",
        val p18: String = "18",
        val p19: String = "19",
        val p20: String = "20",
        val p21: String = "21",
        val p22: String = "22",
        val p23: String = "23",
        val p24: String = "24",
        val p25: String = "25",
        val p26: String = "26",
        val p27: String = "27",
        val p28: String = "28",
        val p29: String = "29",
        val p30: String = "30",
        val p31: String = "31"
    )

    @Test
    fun test32() {
        assertEquals(Dst32(), defaultMapper.readValue<Dst32>("{}"))
    }

    data class Dst33(
        val p00: String = "0",
        val p01: String = "1",
        val p02: String = "2",
        val p03: String = "3",
        val p04: String = "4",
        val p05: String = "5",
        val p06: String = "6",
        val p07: String = "7",
        val p08: String = "8",
        val p09: String = "9",
        val p10: String = "10",
        val p11: String = "11",
        val p12: String = "12",
        val p13: String = "13",
        val p14: String = "14",
        val p15: String = "15",
        val p16: String = "16",
        val p17: String = "17",
        val p18: String = "18",
        val p19: String = "19",
        val p20: String = "20",
        val p21: String = "21",
        val p22: String = "22",
        val p23: String = "23",
        val p24: String = "24",
        val p25: String = "25",
        val p26: String = "26",
        val p27: String = "27",
        val p28: String = "28",
        val p29: String = "29",
        val p30: String = "30",
        val p31: String = "31",
        val p32: String = "32"
    )

    @Test
    fun test33() {
        assertEquals(Dst33(), defaultMapper.readValue<Dst33>("{}"))
    }

    data class Dst64(
        val p00: String = "0",
        val p01: String = "1",
        val p02: String = "2",
        val p03: String = "3",
        val p04: String = "4",
        val p05: String = "5",
        val p06: String = "6",
        val p07: String = "7",
        val p08: String = "8",
        val p09: String = "9",
        val p10: String = "10",
        val p11: String = "11",
        val p12: String = "12",
        val p13: String = "13",
        val p14: String = "14",
        val p15: String = "15",
        val p16: String = "16",
        val p17: String = "17",
        val p18: String = "18",
        val p19: String = "19",
        val p20: String = "20",
        val p21: String = "21",
        val p22: String = "22",
        val p23: String = "23",
        val p24: String = "24",
        val p25: String = "25",
        val p26: String = "26",
        val p27: String = "27",
        val p28: String = "28",
        val p29: String = "29",
        val p30: String = "30",
        val p31: String = "31",
        val p32: String = "32",
        val p33: String = "33",
        val p34: String = "34",
        val p35: String = "35",
        val p36: String = "36",
        val p37: String = "37",
        val p38: String = "38",
        val p39: String = "39",
        val p40: String = "40",
        val p41: String = "41",
        val p42: String = "42",
        val p43: String = "43",
        val p44: String = "44",
        val p45: String = "45",
        val p46: String = "46",
        val p47: String = "47",
        val p48: String = "48",
        val p49: String = "49",
        val p50: String = "50",
        val p51: String = "51",
        val p52: String = "52",
        val p53: String = "53",
        val p54: String = "54",
        val p55: String = "55",
        val p56: String = "56",
        val p57: String = "57",
        val p58: String = "58",
        val p59: String = "59",
        val p60: String = "60",
        val p61: String = "61",
        val p62: String = "62",
        val p63: String = "63"
    )

    @Test
    fun test64() {
        assertEquals(Dst64(), defaultMapper.readValue<Dst64>("{}"))
    }

    data class Dst65(
        val p00: String = "0",
        val p01: String = "1",
        val p02: String = "2",
        val p03: String = "3",
        val p04: String = "4",
        val p05: String = "5",
        val p06: String = "6",
        val p07: String = "7",
        val p08: String = "8",
        val p09: String = "9",
        val p10: String = "10",
        val p11: String = "11",
        val p12: String = "12",
        val p13: String = "13",
        val p14: String = "14",
        val p15: String = "15",
        val p16: String = "16",
        val p17: String = "17",
        val p18: String = "18",
        val p19: String = "19",
        val p20: String = "20",
        val p21: String = "21",
        val p22: String = "22",
        val p23: String = "23",
        val p24: String = "24",
        val p25: String = "25",
        val p26: String = "26",
        val p27: String = "27",
        val p28: String = "28",
        val p29: String = "29",
        val p30: String = "30",
        val p31: String = "31",
        val p32: String = "32",
        val p33: String = "33",
        val p34: String = "34",
        val p35: String = "35",
        val p36: String = "36",
        val p37: String = "37",
        val p38: String = "38",
        val p39: String = "39",
        val p40: String = "40",
        val p41: String = "41",
        val p42: String = "42",
        val p43: String = "43",
        val p44: String = "44",
        val p45: String = "45",
        val p46: String = "46",
        val p47: String = "47",
        val p48: String = "48",
        val p49: String = "49",
        val p50: String = "50",
        val p51: String = "51",
        val p52: String = "52",
        val p53: String = "53",
        val p54: String = "54",
        val p55: String = "55",
        val p56: String = "56",
        val p57: String = "57",
        val p58: String = "58",
        val p59: String = "59",
        val p60: String = "60",
        val p61: String = "61",
        val p62: String = "62",
        val p63: String = "63",
        val p64: String = "64"
    )

    @Test
    fun test65() {
        assertEquals(Dst65(), defaultMapper.readValue<Dst65>("{}"))
    }

    // It cannot be a data class because the generated method would exceed the argument size limit.
    class DstMax(
        val p000: String = "0",
        val p001: String = "1",
        val p002: String = "2",
        val p003: String = "3",
        val p004: String = "4",
        val p005: String = "5",
        val p006: String = "6",
        val p007: String = "7",
        val p008: String = "8",
        val p009: String = "9",
        val p010: String = "10",
        val p011: String = "11",
        val p012: String = "12",
        val p013: String = "13",
        val p014: String = "14",
        val p015: String = "15",
        val p016: String = "16",
        val p017: String = "17",
        val p018: String = "18",
        val p019: String = "19",
        val p020: String = "20",
        val p021: String = "21",
        val p022: String = "22",
        val p023: String = "23",
        val p024: String = "24",
        val p025: String = "25",
        val p026: String = "26",
        val p027: String = "27",
        val p028: String = "28",
        val p029: String = "29",
        val p030: String = "30",
        val p031: String = "31",
        val p032: String = "32",
        val p033: String = "33",
        val p034: String = "34",
        val p035: String = "35",
        val p036: String = "36",
        val p037: String = "37",
        val p038: String = "38",
        val p039: String = "39",
        val p040: String = "40",
        val p041: String = "41",
        val p042: String = "42",
        val p043: String = "43",
        val p044: String = "44",
        val p045: String = "45",
        val p046: String = "46",
        val p047: String = "47",
        val p048: String = "48",
        val p049: String = "49",
        val p050: String = "50",
        val p051: String = "51",
        val p052: String = "52",
        val p053: String = "53",
        val p054: String = "54",
        val p055: String = "55",
        val p056: String = "56",
        val p057: String = "57",
        val p058: String = "58",
        val p059: String = "59",
        val p060: String = "60",
        val p061: String = "61",
        val p062: String = "62",
        val p063: String = "63",
        val p064: String = "64",
        val p065: String = "65",
        val p066: String = "66",
        val p067: String = "67",
        val p068: String = "68",
        val p069: String = "69",
        val p070: String = "70",
        val p071: String = "71",
        val p072: String = "72",
        val p073: String = "73",
        val p074: String = "74",
        val p075: String = "75",
        val p076: String = "76",
        val p077: String = "77",
        val p078: String = "78",
        val p079: String = "79",
        val p080: String = "80",
        val p081: String = "81",
        val p082: String = "82",
        val p083: String = "83",
        val p084: String = "84",
        val p085: String = "85",
        val p086: String = "86",
        val p087: String = "87",
        val p088: String = "88",
        val p089: String = "89",
        val p090: String = "90",
        val p091: String = "91",
        val p092: String = "92",
        val p093: String = "93",
        val p094: String = "94",
        val p095: String = "95",
        val p096: String = "96",
        val p097: String = "97",
        val p098: String = "98",
        val p099: String = "99",
        val p100: String = "100",
        val p101: String = "101",
        val p102: String = "102",
        val p103: String = "103",
        val p104: String = "104",
        val p105: String = "105",
        val p106: String = "106",
        val p107: String = "107",
        val p108: String = "108",
        val p109: String = "109",
        val p110: String = "110",
        val p111: String = "111",
        val p112: String = "112",
        val p113: String = "113",
        val p114: String = "114",
        val p115: String = "115",
        val p116: String = "116",
        val p117: String = "117",
        val p118: String = "118",
        val p119: String = "119",
        val p120: String = "120",
        val p121: String = "121",
        val p122: String = "122",
        val p123: String = "123",
        val p124: String = "124",
        val p125: String = "125",
        val p126: String = "126",
        val p127: String = "127",
        val p128: String = "128",
        val p129: String = "129",
        val p130: String = "130",
        val p131: String = "131",
        val p132: String = "132",
        val p133: String = "133",
        val p134: String = "134",
        val p135: String = "135",
        val p136: String = "136",
        val p137: String = "137",
        val p138: String = "138",
        val p139: String = "139",
        val p140: String = "140",
        val p141: String = "141",
        val p142: String = "142",
        val p143: String = "143",
        val p144: String = "144",
        val p145: String = "145",
        val p146: String = "146",
        val p147: String = "147",
        val p148: String = "148",
        val p149: String = "149",
        val p150: String = "150",
        val p151: String = "151",
        val p152: String = "152",
        val p153: String = "153",
        val p154: String = "154",
        val p155: String = "155",
        val p156: String = "156",
        val p157: String = "157",
        val p158: String = "158",
        val p159: String = "159",
        val p160: String = "160",
        val p161: String = "161",
        val p162: String = "162",
        val p163: String = "163",
        val p164: String = "164",
        val p165: String = "165",
        val p166: String = "166",
        val p167: String = "167",
        val p168: String = "168",
        val p169: String = "169",
        val p170: String = "170",
        val p171: String = "171",
        val p172: String = "172",
        val p173: String = "173",
        val p174: String = "174",
        val p175: String = "175",
        val p176: String = "176",
        val p177: String = "177",
        val p178: String = "178",
        val p179: String = "179",
        val p180: String = "180",
        val p181: String = "181",
        val p182: String = "182",
        val p183: String = "183",
        val p184: String = "184",
        val p185: String = "185",
        val p186: String = "186",
        val p187: String = "187",
        val p188: String = "188",
        val p189: String = "189",
        val p190: String = "190",
        val p191: String = "191",
        val p192: String = "192",
        val p193: String = "193",
        val p194: String = "194",
        val p195: String = "195",
        val p196: String = "196",
        val p197: String = "197",
        val p198: String = "198",
        val p199: String = "199",
        val p200: String = "200",
        val p201: String = "201",
        val p202: String = "202",
        val p203: String = "203",
        val p204: String = "204",
        val p205: String = "205",
        val p206: String = "206",
        val p207: String = "207",
        val p208: String = "208",
        val p209: String = "209",
        val p210: String = "210",
        val p211: String = "211",
        val p212: String = "212",
        val p213: String = "213",
        val p214: String = "214",
        val p215: String = "215",
        val p216: String = "216",
        val p217: String = "217",
        val p218: String = "218",
        val p219: String = "219",
        val p220: String = "220",
        val p221: String = "221",
        val p222: String = "222",
        val p223: String = "223",
        val p224: String = "224",
        val p225: String = "225",
        val p226: String = "226",
        val p227: String = "227",
        val p228: String = "228",
        val p229: String = "229",
        val p230: String = "230",
        val p231: String = "231",
        val p232: String = "232",
        val p233: String = "233",
        val p234: String = "234",
        val p235: String = "235",
        val p236: String = "236",
        val p237: String = "237",
        val p238: String = "238",
        val p239: String = "239",
        val p240: String = "240",
        val p241: String = "241",
        val p242: String = "242",
        val p243: String = "243",
        val p244: String = "244"
    )

    @Test
    fun testMax() {
        assertReflectEquals(DstMax(), defaultMapper.readValue<DstMax>("{}"))
    }
}
