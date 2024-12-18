package es.juliogtrenard.movercaballo

import android.graphics.Point
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    /**
     * Anchura del bonus, de su barra que crece
     */
    private var width_bonus = 0

    /**
     * Guarda las posiciones x e Y de manera temporal
     */
    private var cellSelected_x = 0
    private var cellSelected_y = 0

    /**
     * Los movimientos del usuario hasta terminar
     */
    private var moves = 64

    /**
     * Contado hasta el bonus
     */
    private var bonus = 0

    /**
     * Contiene la matriz del tablero de ajedrez
     */
    private lateinit var tablero: Array<IntArray>

    /**
     * Los movimientos del usuario hasta terminar en este nivel
     */
    private var lvlMoves = 64

    /**
     * Movimientos requeridos hasta recibir un premio
     */
    private var movesRequired = 4

    /**
     * El color para la celda negra
     */
    private var colorCeldaNegra = "black_cell"

    /**
     * El color para la celda blanca
     */
    private var colorCeldaBlanca = "white_cell"

    /**
     * Mira si es necesario o no comprobar el movimiento, si tenemos bonus podemos hacer saltos directos
     */
    private var checkMovement = true

    /**
     *   las que pueden ser en catidad de movimientos
     */

    private var numeroOpcionesDisponibles = 0

    /**
     * Estado del juego, jugando o no
     */
    private var jugando = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inicializarJuego()
    }

    /**
     * Inicialización del juego
     */
    private fun inicializarJuego() {
        // Tamaño del tablero
        setSizeBoard()
        // Ocultar mensaje de vidas y nivel
        hideMessage()

    }

    /**
     * Ajusta el tamaño del tablero
     */
    private fun setSizeBoard() {
        // Recorrer todas las celdas y reasignar altura y anchura
        var iv: ImageView

        // Características de la pantalla
        val display =  windowManager.defaultDisplay

        // Variable para guardar su tamaño
        val size = Point()

        // Tamaño de la pantalla
        display.getSize(size)

        // El ancho total de la pantalla es del punto size la parte x
        val width = size.x

        // El ancho expresado en dp es el resultado del ancho entre la densidad de la pantalla del telefono
        val widthDp = (width / getResources().displayMetrics.density)

        // Tamaño para el margen lateral
        val lateralMarginDP = 0
        val widthCell = (widthDp - lateralMarginDP) / 8

        // Como altura y anchura son iguales, solo necesitamos una
        val heightCell = widthCell

        // El tamaño del bonus es el doble que el de las celdas
        width_bonus = widthCell.toInt() * 2

        for (i in 0..7) {
            for (j in 0..7) {
                iv = findViewById(resources.getIdentifier("ivc$i$j", "id", packageName))

                // Asignar los nuevos anchos y largos a cada celda
                val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightCell, getResources().displayMetrics).toInt()
                val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthCell, getResources().displayMetrics).toInt()

                iv.layoutParams = TableRow.LayoutParams(width, height)
            }
        }
    }

    /**
     * Oculta el layout de las vidas y el nivel
     */
    private fun hideMessage() {
        val lyMessage: LinearLayout = findViewById(R.id.llMensaje)
        lyMessage.visibility = View.INVISIBLE
    }

    /**
     * Para que el juego siempre sea diferente se posiciona inicialmente el caballo en un sitio o
     * en otro de manera aleatoria
     */
    private fun setFirstPosition() {
        val x = (0..7).random()
        val y = (0..7).random()

        // Recordamos las que hemos pintado
        cellSelected_x = x
        cellSelected_y = y

        // Con esa posición aleatoria, pinta el caballo
        selectCell(x,y)
    }

    /**
     * Selecciona una celda dada la posición y pinta el caballo en ella
     * nunca debe ser mayor que el rango 0..7
     * @param x fila
     * @param y columna
     */
    private fun selectCell(x: Int, y: Int) {
        // Ha pulsado, una posición menos para llenar
        moves--
        val tv = findViewById<TextView>(R.id.movimientosDatos)
        tv.text = "$moves"

        // Dibuja la barra horizontal de bonus en función de los puntos que falten para nuevo bonus
        refrescarBarraBonus()

        // Hemos caido en un bonus?
        if (tablero[x][y] == 2) {
            bonus++
            val tvBonusDato = findViewById<TextView>(R.id.bonusDato)

            // Concatena la info de bonus
            tvBonusDato.text = "  +" + bonus.toString()
        }

        // Señalizamos en la matriz que en esa posición hay un caballo
        tablero[x][y] = 1

        // Como sé cual era la celda anterior pues la he guardado en setFirtPosition antes de llamar
        // la pinto como usada
        pintarCaballoEnCelda(cellSelected_x, cellSelected_y, "previous_cell")

        // La nueva posición anterior sera justo la que vamos a pintar ahora
        cellSelected_x = x
        cellSelected_y = y

        // Borramos las opciones posibles anteriores
        borrarOpcionesAntiguas()

        // Ahora pinto la nueva celda
        pintarCaballoEnCelda(x, y, "selected_cell")

        // Despues de pintar que vuelva a fijar checkMovement a true
        checkMovement = true

        // Revisamos las posibles opciones de movimiento
        checkPosiblesOpciones(x,y)

        // Si aún quedan movimientos por hacer
        if (moves > 0) {
            // Mirar si hay premio
            checkNuevoBonus()
            // Mirar si es fin de partida
            checkGameOver()
        }
        else mostrarMensaje( "Has ganado","Muy bien",false)
    }

    /**
     * En función de los puntos que faltan para bonus, hace crecer la barra horizontal de bonus
     */
    private fun refrescarBarraBonus() {
        // Calculamos la anchura proporcional
        val movesDone = lvlMoves - moves
        val bonusDone = movesDone / movesRequired
        val movesRest = movesRequired * (bonusDone)
        val bonusGrow = movesDone - movesRest

        // Voy a trabajar sobre este view de vNuevoBonus
        val v = findViewById<View>(R.id.vNuevoBonus)

        // El nuevo ancho se calculará así
        val widthBonus = ((width_bonus/movesRequired) * bonusGrow).toFloat()

        // Recojo el tamaño del view que usaremos como barra
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().displayMetrics).toInt()
        val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthBonus, getResources().displayMetrics).toInt()

        // Cambiamos el parámetro de su ancho y largo, dibujándolo efectivamente
        v.layoutParams = TableRow.LayoutParams(width, height)
    }

    /**
     * Dibuja el caballo en la celda dada
     * @param x
     * @param y
     * @param color
     */
    private fun pintarCaballoEnCelda(x: Int, y: Int, color: String) {
        val iv: ImageView = findViewById(resources.getIdentifier("ivc$x$y", "id", packageName))

        // Cambiamos el color de fondo, se referencia mediante ContextCompact
        iv.setBackgroundColor(ContextCompat.getColor(this, resources.getIdentifier(color, "color", packageName)))

        // Pintamos el caballo directamente asignando el recurso de imagen
        iv.setImageResource(R.drawable.caballo)
    }

    /**
     * Reinicia en el tablero las posibles opciones de movimiento pintadas anteriormente
     * es decir cambiamos las tipo 9 a tipo anterior
     */
    private fun borrarOpcionesAntiguas() {
        for (i in 0..7){
            for (j in 0..7) {
                if (tablero[i][j] == 9 || tablero[i][j] == 2) {
                    if (tablero[i][j] == 9) tablero[i][j] = 0
                    borrarOpcionAntigua(i, j)
                }
            }
        }
    }

    /**
     * Limpia una celda concreta
     * @param x posición x de la celda a limpiar
     * @param y posición y de la celda a limpiar
     */
    private fun borrarOpcionAntigua(x: Int, y: Int) {
        // Recogemos una celda
        val iv : ImageView = findViewById(resources.getIdentifier("ivc$x$y", "id", packageName))

        if (mirarColor(x,y) == "negra")
            iv.setBackgroundColor(ContextCompat.getColor(this,
                resources.getIdentifier(colorCeldaNegra, "color", packageName)))
        else
            iv.setBackgroundColor(ContextCompat.getColor(this,
                resources.getIdentifier(colorCeldaBlanca, "color", packageName)))

        if (tablero[x][y] == 1)
            iv.setBackgroundColor(ContextCompat.getColor(this,
            resources.getIdentifier("previous_cell", "color", packageName)))
    }

    /**
     * Devuelve el color de una celda en forma de cadena
     * @param x la posición x de la celda a mirar
     * @param y la posición y de la celda a mirar
     * @return el color de esa celda como "negra" o "blanca"
     */
    private fun mirarColor(x: Int, y: Int): String {
        var color = ""

        // Las columnas negras son estas
        val columnaNegra = arrayOf(0,2,4,6)

        // Las filas negras son estas
        val filaNegra = arrayOf(1,3,5,7)

        // Si coincide en esa posición
        color = if ((columnaNegra.contains(x) && columnaNegra.contains(y))
            || (filaNegra.contains(x) && filaNegra.contains(y)))
            "negra"
        else "blanca"

        return color
    }

    /**
     * En función de la posición dada y teniendo en cuenta lo que queda libre del tablero, cuenta
     * los movimientos posibles de un solo salto
     * @param x posición en x
     * @param y posición en y
     * @return cantidad opciones
     */
    private fun checkPosiblesOpciones(x: Int, y: Int) : Int {
        // Las que pueden ser
        numeroOpcionesDisponibles = 0

        // Cuento los posibles y los voy sumando
        // numeroOpcionesDisponibles =
        checkMovimiento(x,y,1,2)
        checkMovimiento(x,y,1,-2)
        checkMovimiento(x,y,2,1)
        checkMovimiento(x,y,2,-1)
        checkMovimiento(x,y,-1,2)
        checkMovimiento(x,y,-1,-2)
        checkMovimiento(x,y,-2,1)
        checkMovimiento(x,y,-2,-1)

        // De paso actualizo el campo opciones disponibles de la interfaz
        val tv = findViewById<TextView>(R.id.opcionesDato)
        tv.text = "$numeroOpcionesDisponibles"

        //devuelvo la cantidad posible
        return numeroOpcionesDisponibles
    }

    /**
     * True si el movimiento es posible
     * @param x el parámetro x en el que se encuentra actualmente
     * @param y el parámetro y en el que se encuentra actualmente
     * @param i la posición x a la que quiere saltar
     * @param j la posición y a la que quiere saltar
     * @return 1 para libre, 0 para ocupado asi puedo hacer una suma total al llamar a esto por
     * las diferentes celdas
     */
    private fun checkMovimiento(x: Int, y: Int, i: Int, j: Int) {
        val opcionX = x + i
        val opcionY = y + j

        if (opcionX < 8 && opcionY < 8 && opcionX >= 0 && opcionY >= 0){
            if ((tablero[opcionX][opcionY] == 0)
                || (tablero[opcionX][opcionY] == 2)) {
                numeroOpcionesDisponibles++
                refrescaOpciones(opcionX,opcionY)
                // Si es una casilla vacia, entonces se puede marcar como premio
                if(tablero[opcionX][opcionY] == 0) tablero[opcionX][opcionY] = 9
            }
        }
    }

    /**
     * Redibuja el campo opciones con las opciones nuevas
     */
    private fun refrescaOpciones(x: Int, y: Int) {
        // Vamos a dar un borde extra para que se vea en el tablero
        val iv : ImageView = findViewById(resources.getIdentifier("ivc$x$y", "id", packageName))

        // El reborde depende del color en función del color de la propia celda
        // sera reborde opcionblanca.xml o opcionnegra.xml
        if (mirarColor(x,y) =="negra"){
            iv.setBackgroundResource(R.drawable.opcionblanca)
        } else {
            //debe ser negra, añade blanca
            iv.setBackgroundResource(R.drawable.opcionnegra)
        }
    }

    /**
     * Tienes bonus, puedes seguir
     */
    private fun checkNuevoBonus() {
        if (moves%movesRequired == 0) { // En bloques de cada 4
            // Creo un nuevo punto
            var bonusCellX = 0
            var bonusCellY = 0
            var bonusCell = false

            while (!bonusCell) {
                // Hasta que no encuentre una celda para bonus buena
                bonusCellX = (0..7).random()
                bonusCellY = (0..7).random()

                // Como esta libre, dejamos de buscar
                if(tablero[bonusCellX][bonusCellY] == 0) bonusCell = true
            }

            // Le damos el valor de bonus, recuerda 0 libre, 9 opcion y 2 bonus
            tablero[bonusCellX][bonusCellY] = 2

            // Pintamos el bonus
            pintarBonusCell(bonusCellX, bonusCellY)
        }
    }

    /**
     * Pinta un bonus en la celda que le digas
     */
    private fun pintarBonusCell(bonusCellX: Int, bonusCellY: Int) {
        val iv : ImageView =
            findViewById(resources.getIdentifier("ivc$bonusCellX$bonusCellY", "id", packageName))

        // Sobre esa celda iv pintamos el bonus
        iv.setImageResource(R.drawable.bonus)
    }

    /**
     * Desde una posición dada comprueba si aún quedan movimientos posibles
     * en función de ello es gameOver o no
     */
    private fun checkGameOver() {
        if (numeroOpcionesDisponibles == 0) {
            if (bonus > 0) {
                // Como si tiene bonus puede hacer un salto directo, no mires si es un movimiento legal o no
                checkMovement = false

                // Enseñame todas las opciones de salto
                paintAllOptions()
            } else {
                // También estás en bonus cero
                mostrarMensaje( "FIN DE JUEGO", "Mala suerte", true) // Le pasamos el recurso del string finDeJuego
            }
        }
    }

    /**
     * Dibuja todas las opciones que no esten libres
     */
    private fun paintAllOptions() {
        // Recorremos el tablero completo y en las posiciones cero les damos un resalto
        for (i in 0..7){
            for (j in 0..7) {
                // Lo que no sea 1, es decir, bonus, opción o cero
                if (tablero[i][j] != 1) refrescaOpciones(i, j)
                if (tablero[i][j] == 0) tablero[i][j] = 9
            }
        }
    }

    /**
     * Muestra la pantalla de información para nuevo nivel o de game over
     * @param mensaje la frase a mostrar
     * @param subtexto segunda frase a mostrar
     * @param esGameOver si es true, es que es mensaje de gameOver, si es false, es nextlevel
     */
    private fun mostrarMensaje(mensaje: String, subtexto: String, esGameOver:Boolean) {
        // Ya no estas jugando
        jugando = false

        // Busco el layout de mensaje
        val llMensaje = findViewById<LinearLayout>(R.id.llMensaje)

        //Busco los textviews de ese panel
        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        val tvTitleMessage = findViewById<TextView>(R.id.tvTitleMesage)
        val tvScore = findViewById<TextView>(R.id.tvScore)

        llMensaje.visibility = View.VISIBLE

        // Si es gameover que se vean sus puntos si no el tiempo del nivel
        val puntos : String
        if (esGameOver) {
            puntos = "Puntos: " + (lvlMoves - moves) + "/" + lvlMoves

        } else {
            val tiempo = "0:00"
            puntos = "Tiempo: $tiempo"
        }
        // Llenamos los textos
        tvMessage.text = mensaje
        tvTitleMessage.text = subtexto
        tvScore.text = puntos
    }
}