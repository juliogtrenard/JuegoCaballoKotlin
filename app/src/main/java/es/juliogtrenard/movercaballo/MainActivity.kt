package es.juliogtrenard.movercaballo

import android.graphics.Point
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    /**
     * Anchura del bonus, de su barra que crece
     */
    private var width_bonus = 0

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
}