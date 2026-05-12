package com.marujho.freshsnap.data.domain

import com.marujho.freshsnap.data.model.ShoppingCategory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clasifica un producto en una categoria a partir de palabras clave en su nombre.
 *
 * Estrategia:
 *  1. Normaliza (lowercase + quita acentos).
 *  2. Tokeniza por espacios/comas.
 *  3. Para cada categoria, calcula un score:
 *      - keyword multi-palabra (ej. "tomate frito") -> contains() vale 2 puntos
 *      - keyword de una palabra -> match exacto contra un token vale 1 punto
 *  4. Gana la categoria con mas score. Empate -> primera en orden de declaracion.
 *  5. Sin matches -> OTHER.
 *
 * Esto evita falsos positivos del enfoque anterior (substring puro): por ejemplo
 * "estepa" no matchea "te", y "mostaza" no matchea "mostarda".
 */
@Singleton
class ShoppingCategoryClassifier @Inject constructor() {

    private val keywords: Map<ShoppingCategory, List<String>> = mapOf(
        ShoppingCategory.DAIRY to listOf(
            "leche", "queso", "yogur", "yogurt", "mantequilla", "nata", "kefir",
            "cuajada", "requeson", "mascarpone", "mozzarella", "cheddar", "parmesano",
            "burrata", "feta", "manchego", "ricotta"
        ),
        ShoppingCategory.VEGETABLES to listOf(
            "tomate", "cebolla", "lechuga", "patata", "papa", "patatas", "zanahoria",
            "pimiento", "pimientos", "ajo", "calabacin", "calabaza", "berenjena",
            "espinaca", "espinacas", "puerro", "pepino", "brocoli", "coliflor",
            "col", "apio", "champinon", "champinones", "seta", "setas", "esparrago",
            "esparragos", "rabano", "remolacha", "acelga", "judia", "judias",
            "guisante", "guisantes", "alcachofa", "alcachofas", "rucula", "canonigos",
            "endivia", "perejil", "cilantro", "menta", "albahaca", "tomate frito"
        ),
        ShoppingCategory.FRUITS to listOf(
            "manzana", "manzanas", "pera", "peras", "platano", "platanos", "banana",
            "naranja", "naranjas", "mandarina", "mandarinas", "limon", "limones",
            "fresa", "fresas", "uva", "uvas", "kiwi", "melon", "sandia", "pina",
            "mango", "aguacate", "aguacates", "melocoton", "albaricoque", "cereza",
            "cerezas", "ciruela", "ciruelas", "higo", "higos", "granada",
            "frambuesa", "arandano", "arandanos", "mora", "moras"
        ),
        ShoppingCategory.MEAT to listOf(
            "pollo", "ternera", "cerdo", "cordero", "pavo", "atun", "salmon",
            "merluza", "bacalao", "gamba", "gambas", "marisco", "mariscos",
            "jamon", "chorizo", "salchicha", "salchichas", "longaniza", "morcilla",
            "lomo", "filete", "albondiga", "albondigas", "pescado", "calamar",
            "calamares", "pulpo", "sardina", "sardinas", "trucha", "lubina",
            "dorada", "rape", "boqueron", "boquerones", "anchoa", "anchoas",
            "panceta", "bacon", "carne picada", "solomillo", "costilla", "costillas"
        ),
        ShoppingCategory.BAKERY to listOf(
            "pan", "barra", "croissant", "magdalena", "magdalenas", "bizcocho",
            "donut", "donuts", "tostada", "tostadas", "panecillo", "panecillos",
            "bollo", "bollos", "ensaimada", "galleta", "galletas", "muffin",
            "brioche", "baguette"
        ),
        ShoppingCategory.PANTRY to listOf(
            "arroz", "pasta", "espagueti", "macarrones", "fideos", "legumbre",
            "garbanzo", "garbanzos", "lenteja", "lentejas", "alubia", "alubias",
            "harina", "azucar", "sal", "aceite", "vinagre", "pimienta", "especias",
            "pimenton", "tomate frito", "salsa", "soja", "ketchup", "mayonesa",
            "mostaza", "mostarda", "miel", "mermelada", "cereal", "cereales",
            "muesli", "avena", "chocolate", "cacao", "nuez", "nueces", "almendra",
            "almendras", "pasas", "conserva", "lata", "vinagreta", "alioli",
            "tabasco", "wasabi", "curry", "comino", "oregano", "tomillo", "romero",
            "laurel", "azafran", "canela", "vainilla", "levadura", "polvo de hornear"
        ),
        ShoppingCategory.DRINKS to listOf(
            "agua", "zumo", "zumos", "refresco", "refrescos", "cerveza", "cervezas",
            "vino", "vinos", "cafe", "te", "infusion", "infusiones", "cola",
            "fanta", "sprite", "tonica", "coca", "champan", "cava", "sidra",
            "vermut", "ginebra", "ron", "whisky", "vodka", "tequila"
        ),
        ShoppingCategory.FROZEN to listOf(
            "helado", "helados", "polo", "polos", "congelado", "congelados",
            "pizza congelada", "verdura congelada", "nuggets congelados",
            "patatas fritas congeladas"
        ),
        ShoppingCategory.CLEANING to listOf(
            "detergente", "lejia", "papel higienico", "papel de cocina", "jabon",
            "fregona", "esponja", "estropajo", "bayeta", "ambientador",
            "lavavajillas", "suavizante", "limpiador", "amoniaco",
            "bolsa de basura", "papel de aluminio", "film", "servilleta",
            "servilletas"
        )
    )

    /** Quita acentos basicos para que "platano" matchee "plátano". */
    private fun normalize(s: String): String =
        s.lowercase()
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
            .trim()

    fun classify(name: String): ShoppingCategory {
        val normalized = normalize(name)
        if (normalized.isBlank()) return ShoppingCategory.OTHER

        val tokens = normalized.split(Regex("[\\s,]+")).filter { it.isNotBlank() }

        var bestCategory = ShoppingCategory.OTHER
        var bestScore = 0

        for ((category, words) in keywords) {
            var score = 0
            for (keyword in words) {
                val k = normalize(keyword)
                if (k.contains(" ")) {
                    // Multi-palabra: substring directo contra el nombre completo
                    if (normalized.contains(k)) score += 2
                } else {
                    // Una palabra: match exacto contra los tokens
                    if (tokens.any { it == k }) score += 1
                }
            }
            if (score > bestScore) {
                bestScore = score
                bestCategory = category
            }
        }
        return bestCategory
    }
}
