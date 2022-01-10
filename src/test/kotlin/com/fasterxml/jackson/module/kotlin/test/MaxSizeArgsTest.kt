package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

// Test for constructor/factory functions with the largest argument size
class MaxSizeArgsTest {
    data class MaxSizeConstructor(
        val arg000: Int, val arg001: Int, val arg002: Int, val arg003: Int, val arg004: Int,
        val arg005: Int, val arg006: Int, val arg007: Int, val arg008: Int, val arg009: Int,
        val arg010: Int, val arg011: Int, val arg012: Int, val arg013: Int, val arg014: Int,
        val arg015: Int, val arg016: Int, val arg017: Int, val arg018: Int, val arg019: Int,
        val arg020: Int, val arg021: Int, val arg022: Int, val arg023: Int, val arg024: Int,
        val arg025: Int, val arg026: Int, val arg027: Int, val arg028: Int, val arg029: Int,
        val arg030: Int, val arg031: Int, val arg032: Int, val arg033: Int, val arg034: Int,
        val arg035: Int, val arg036: Int, val arg037: Int, val arg038: Int, val arg039: Int,
        val arg040: Int, val arg041: Int, val arg042: Int, val arg043: Int, val arg044: Int,
        val arg045: Int, val arg046: Int, val arg047: Int, val arg048: Int, val arg049: Int,
        val arg050: Int, val arg051: Int, val arg052: Int, val arg053: Int, val arg054: Int,
        val arg055: Int, val arg056: Int, val arg057: Int, val arg058: Int, val arg059: Int,
        val arg060: Int, val arg061: Int, val arg062: Int, val arg063: Int, val arg064: Int,
        val arg065: Int, val arg066: Int, val arg067: Int, val arg068: Int, val arg069: Int,
        val arg070: Int, val arg071: Int, val arg072: Int, val arg073: Int, val arg074: Int,
        val arg075: Int, val arg076: Int, val arg077: Int, val arg078: Int, val arg079: Int,
        val arg080: Int, val arg081: Int, val arg082: Int, val arg083: Int, val arg084: Int,
        val arg085: Int, val arg086: Int, val arg087: Int, val arg088: Int, val arg089: Int,
        val arg090: Int, val arg091: Int, val arg092: Int, val arg093: Int, val arg094: Int,
        val arg095: Int, val arg096: Int, val arg097: Int, val arg098: Int, val arg099: Int,
        val arg100: Int, val arg101: Int, val arg102: Int, val arg103: Int, val arg104: Int,
        val arg105: Int, val arg106: Int, val arg107: Int, val arg108: Int, val arg109: Int,
        val arg110: Int, val arg111: Int, val arg112: Int, val arg113: Int, val arg114: Int,
        val arg115: Int, val arg116: Int, val arg117: Int, val arg118: Int, val arg119: Int,
        val arg120: Int, val arg121: Int, val arg122: Int, val arg123: Int, val arg124: Int,
        val arg125: Int, val arg126: Int, val arg127: Int, val arg128: Int, val arg129: Int,
        val arg130: Int, val arg131: Int, val arg132: Int, val arg133: Int, val arg134: Int,
        val arg135: Int, val arg136: Int, val arg137: Int, val arg138: Int, val arg139: Int,
        val arg140: Int, val arg141: Int, val arg142: Int, val arg143: Int, val arg144: Int,
        val arg145: Int, val arg146: Int, val arg147: Int, val arg148: Int, val arg149: Int,
        val arg150: Int, val arg151: Int, val arg152: Int, val arg153: Int, val arg154: Int,
        val arg155: Int, val arg156: Int, val arg157: Int, val arg158: Int, val arg159: Int,
        val arg160: Int, val arg161: Int, val arg162: Int, val arg163: Int, val arg164: Int,
        val arg165: Int, val arg166: Int, val arg167: Int, val arg168: Int, val arg169: Int,
        val arg170: Int, val arg171: Int, val arg172: Int, val arg173: Int, val arg174: Int,
        val arg175: Int, val arg176: Int, val arg177: Int, val arg178: Int, val arg179: Int,
        val arg180: Int, val arg181: Int, val arg182: Int, val arg183: Int, val arg184: Int,
        val arg185: Int, val arg186: Int, val arg187: Int, val arg188: Int, val arg189: Int,
        val arg190: Int, val arg191: Int, val arg192: Int, val arg193: Int, val arg194: Int,
        val arg195: Int, val arg196: Int, val arg197: Int, val arg198: Int, val arg199: Int,
        val arg200: Int, val arg201: Int, val arg202: Int, val arg203: Int, val arg204: Int,
        val arg205: Int, val arg206: Int, val arg207: Int, val arg208: Int, val arg209: Int,
        val arg210: Int, val arg211: Int, val arg212: Int, val arg213: Int, val arg214: Int,
        val arg215: Int, val arg216: Int, val arg217: Int, val arg218: Int, val arg219: Int,
        val arg220: Int, val arg221: Int, val arg222: Int, val arg223: Int, val arg224: Int,
        val arg225: Int, val arg226: Int, val arg227: Int, val arg228: Int, val arg229: Int,
        val arg230: Int, val arg231: Int, val arg232: Int, val arg233: Int, val arg234: Int,
        val arg235: Int, val arg236: Int, val arg237: Int, val arg238: Int, val arg239: Int,
        val arg240: Int, val arg241: Int, val arg242: Int, val arg243: Int, val arg244: Int
    ) {
        companion object {
            val defaultInstance: MaxSizeConstructor = ::MaxSizeConstructor.let { function ->
                val arguments = function.parameters.map { it.index }.toTypedArray()
                function.call(*arguments)
            }
        }
    }

    @Test
    fun maxConstructorTest() {
        val mapper = jacksonObjectMapper()
        val actual = mapper.readValue<MaxSizeConstructor>(mapper.writeValueAsString(MaxSizeConstructor.defaultInstance))

        assertEquals(MaxSizeConstructor.defaultInstance, actual)
    }

    data class MaxSizeFunction(val int: Int) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun creator(
                arg000: Int, arg001: Int, arg002: Int, arg003: Int, arg004: Int,
                arg005: Int, arg006: Int, arg007: Int, arg008: Int, arg009: Int,
                arg010: Int, arg011: Int, arg012: Int, arg013: Int, arg014: Int,
                arg015: Int, arg016: Int, arg017: Int, arg018: Int, arg019: Int,
                arg020: Int, arg021: Int, arg022: Int, arg023: Int, arg024: Int,
                arg025: Int, arg026: Int, arg027: Int, arg028: Int, arg029: Int,
                arg030: Int, arg031: Int, arg032: Int, arg033: Int, arg034: Int,
                arg035: Int, arg036: Int, arg037: Int, arg038: Int, arg039: Int,
                arg040: Int, arg041: Int, arg042: Int, arg043: Int, arg044: Int,
                arg045: Int, arg046: Int, arg047: Int, arg048: Int, arg049: Int,
                arg050: Int, arg051: Int, arg052: Int, arg053: Int, arg054: Int,
                arg055: Int, arg056: Int, arg057: Int, arg058: Int, arg059: Int,
                arg060: Int, arg061: Int, arg062: Int, arg063: Int, arg064: Int,
                arg065: Int, arg066: Int, arg067: Int, arg068: Int, arg069: Int,
                arg070: Int, arg071: Int, arg072: Int, arg073: Int, arg074: Int,
                arg075: Int, arg076: Int, arg077: Int, arg078: Int, arg079: Int,
                arg080: Int, arg081: Int, arg082: Int, arg083: Int, arg084: Int,
                arg085: Int, arg086: Int, arg087: Int, arg088: Int, arg089: Int,
                arg090: Int, arg091: Int, arg092: Int, arg093: Int, arg094: Int,
                arg095: Int, arg096: Int, arg097: Int, arg098: Int, arg099: Int,
                arg100: Int, arg101: Int, arg102: Int, arg103: Int, arg104: Int,
                arg105: Int, arg106: Int, arg107: Int, arg108: Int, arg109: Int,
                arg110: Int, arg111: Int, arg112: Int, arg113: Int, arg114: Int,
                arg115: Int, arg116: Int, arg117: Int, arg118: Int, arg119: Int,
                arg120: Int, arg121: Int, arg122: Int, arg123: Int, arg124: Int,
                arg125: Int, arg126: Int, arg127: Int, arg128: Int, arg129: Int,
                arg130: Int, arg131: Int, arg132: Int, arg133: Int, arg134: Int,
                arg135: Int, arg136: Int, arg137: Int, arg138: Int, arg139: Int,
                arg140: Int, arg141: Int, arg142: Int, arg143: Int, arg144: Int,
                arg145: Int, arg146: Int, arg147: Int, arg148: Int, arg149: Int,
                arg150: Int, arg151: Int, arg152: Int, arg153: Int, arg154: Int,
                arg155: Int, arg156: Int, arg157: Int, arg158: Int, arg159: Int,
                arg160: Int, arg161: Int, arg162: Int, arg163: Int, arg164: Int,
                arg165: Int, arg166: Int, arg167: Int, arg168: Int, arg169: Int,
                arg170: Int, arg171: Int, arg172: Int, arg173: Int, arg174: Int,
                arg175: Int, arg176: Int, arg177: Int, arg178: Int, arg179: Int,
                arg180: Int, arg181: Int, arg182: Int, arg183: Int, arg184: Int,
                arg185: Int, arg186: Int, arg187: Int, arg188: Int, arg189: Int,
                arg190: Int, arg191: Int, arg192: Int, arg193: Int, arg194: Int,
                arg195: Int, arg196: Int, arg197: Int, arg198: Int, arg199: Int,
                arg200: Int, arg201: Int, arg202: Int, arg203: Int, arg204: Int,
                arg205: Int, arg206: Int, arg207: Int, arg208: Int, arg209: Int,
                arg210: Int, arg211: Int, arg212: Int, arg213: Int, arg214: Int,
                arg215: Int, arg216: Int, arg217: Int, arg218: Int, arg219: Int,
                arg220: Int, arg221: Int, arg222: Int, arg223: Int, arg224: Int,
                arg225: Int, arg226: Int, arg227: Int, arg228: Int, arg229: Int,
                arg230: Int, arg231: Int, arg232: Int, arg233: Int, arg234: Int,
                arg235: Int, arg236: Int, arg237: Int, arg238: Int, arg239: Int,
                arg240: Int, arg241: Int, arg242: Int, arg243: Int, arg244: Int,
                arg245: Int, arg246: Int, arg247: Int, arg248: Int, arg249: Int,
                arg250: Int, arg251: Int, arg252: Int, arg253: Int
            ) = MaxSizeFunction(
                arg000 + arg001 + arg002 + arg003 + arg004 + arg005 + arg006 + arg007 + arg008 + arg009 +
                arg010 + arg011 + arg012 + arg013 + arg014 + arg015 + arg016 + arg017 + arg018 + arg019 +
                arg020 + arg021 + arg022 + arg023 + arg024 + arg025 + arg026 + arg027 + arg028 + arg029 +
                arg030 + arg031 + arg032 + arg033 + arg034 + arg035 + arg036 + arg037 + arg038 + arg039 +
                arg040 + arg041 + arg042 + arg043 + arg044 + arg045 + arg046 + arg047 + arg048 + arg049 +
                arg050 + arg051 + arg052 + arg053 + arg054 + arg055 + arg056 + arg057 + arg058 + arg059 +
                arg060 + arg061 + arg062 + arg063 + arg064 + arg065 + arg066 + arg067 + arg068 + arg069 +
                arg070 + arg071 + arg072 + arg073 + arg074 + arg075 + arg076 + arg077 + arg078 + arg079 +
                arg080 + arg081 + arg082 + arg083 + arg084 + arg085 + arg086 + arg087 + arg088 + arg089 +
                arg090 + arg091 + arg092 + arg093 + arg094 + arg095 + arg096 + arg097 + arg098 + arg099 +
                arg100 + arg101 + arg102 + arg103 + arg104 + arg105 + arg106 + arg107 + arg108 + arg109 +
                arg110 + arg111 + arg112 + arg113 + arg114 + arg115 + arg116 + arg117 + arg118 + arg119 +
                arg120 + arg121 + arg122 + arg123 + arg124 + arg125 + arg126 + arg127 + arg128 + arg129 +
                arg130 + arg131 + arg132 + arg133 + arg134 + arg135 + arg136 + arg137 + arg138 + arg139 +
                arg140 + arg141 + arg142 + arg143 + arg144 + arg145 + arg146 + arg147 + arg148 + arg149 +
                arg150 + arg151 + arg152 + arg153 + arg154 + arg155 + arg156 + arg157 + arg158 + arg159 +
                arg160 + arg161 + arg162 + arg163 + arg164 + arg165 + arg166 + arg167 + arg168 + arg169 +
                arg170 + arg171 + arg172 + arg173 + arg174 + arg175 + arg176 + arg177 + arg178 + arg179 +
                arg180 + arg181 + arg182 + arg183 + arg184 + arg185 + arg186 + arg187 + arg188 + arg189 +
                arg190 + arg191 + arg192 + arg193 + arg194 + arg195 + arg196 + arg197 + arg198 + arg199 +
                arg200 + arg201 + arg202 + arg203 + arg204 + arg205 + arg206 + arg207 + arg208 + arg209 +
                arg210 + arg211 + arg212 + arg213 + arg214 + arg215 + arg216 + arg217 + arg218 + arg219 +
                arg220 + arg221 + arg222 + arg223 + arg224 + arg225 + arg226 + arg227 + arg228 + arg229 +
                arg230 + arg231 + arg232 + arg233 + arg234 + arg235 + arg236 + arg237 + arg238 + arg239 +
                arg240 + arg241 + arg242 + arg243 + arg244 + arg245 + arg246 + arg247 + arg248 + arg249 +
                arg250 + arg251 + arg252 + arg253
            )
        }
    }

    @Test
    fun maxSizeFunctionTest() {
        val mapper = jacksonObjectMapper()
        val src = (0..253).associateBy { "arg${"%03d".format(it)}" }.let { mapper.writeValueAsString(it) }

        val actual = mapper.readValue<MaxSizeFunction>(src)
        assertEquals(MaxSizeFunction((0..253).sum()), actual)
    }
}
