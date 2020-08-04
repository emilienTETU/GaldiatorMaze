package com.example.gladiateur

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.math.sign


class  GameView(context: Context?, attrs: AttributeSet?) : View(context, attrs){

    private enum class Direction{
        UP,DOWN,LEFT,RIGHT
    }

    private var cellSize = 0
    private var hMargin = 0
    private var vMargin = 0
    private var wallPaint = Paint()
    private var COLS = 2
    private var ROW = 2
    private lateinit var cells : Array<Array<Cell?>>
    private var playerCanMove = true
    private var maze = 1

    private lateinit var player : Cell
    private var playerPaint = Paint()
    private lateinit var glad : Cell
    private var gladPaint = Paint()
    private val textPaint = Paint()

    private val job: Job = Job()
    private val coroutineContext: CoroutineContext = job + Dispatchers.IO
    private val coroutineScope = CoroutineScope(coroutineContext)

    var x1 = 0.0f
    var x2 = 0.0f
    var y1 = 0.0f
    var y2 = 0.0f

    companion object {
        const val WALL_THICKNESS : Float = 6F
        const val DEBUG_TAG = "TEST"
        const val MIN_DISTANCE = 150
    }

    init {
        wallPaint.color = context?.let {
            it.getColor(R.color.black)
        }?: Color.BLACK
        wallPaint.strokeWidth = WALL_THICKNESS

        playerPaint.color = context?.let {
            it.getColor(R.color.orange)
        }?: Color.BLUE
        gladPaint.color = context?.let {
            it.getColor(R.color.gray)
        }?: Color.RED

        Mode.mazeSelected.observe(context?.lifecycleOwner()!!, Observer {
            maze = it
            createMaze()
            invalidate()
        })
        createMaze()
    }

    private fun createMaze(){
        if (Mode.mode != GameMode.EXTREME){
            when(maze){
                1 -> createMaze1()
                2 -> createMaze2()
                3 -> createMaze3()
                4 -> createMaze4()
                5 -> createMaze5()
                6 -> createMaze6()
                7 -> createMaze7()
                8 -> createMaze8()
                9 -> createMaze9()
                10 -> createMaze10()
                11 -> createMaze11()
                12 -> createMaze12()
                13 -> createMazeBonus()
                else -> {
                    Mode.mazeSelected.postValue(1)
                    createMaze1()
                }
            }
        } else{
            createMazeRandom()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var isVictory = true
        when(event.action){
            //When we start to swipe
            ACTION_DOWN -> {
                x1 = event.x
                y1 = event.y
            }
            //When we end the swipe
            ACTION_UP -> {
                x2 = event.x
                y2 = event.y

                val valueX: Float = x2-x1
                val valueY: Float = y2-y1

                if (abs(valueX) > MIN_DISTANCE){
                     if (x2 > x1){
                         isVictory = movePlayer(Direction.RIGHT)
                     }
                    else{
                         isVictory = movePlayer(Direction.LEFT)
                     }
                }
                else if (abs(valueY) > MIN_DISTANCE){
                    if(y2 > y1){
                        isVictory = movePlayer(Direction.DOWN)
                    }
                    else{
                        isVictory = movePlayer(Direction.UP)
                    }
                }
            }
        }
        if (!isVictory){
            moveGlad()
        }
        return true
    }

    private fun movePlayer(dir : Direction) : Boolean{
        var victory = false
        if (playerCanMove) {
            when (dir) {
                Direction.UP -> {
                    if (!player.topWall) {
                        victory = checkVictory(player.y - 1, ROW)
                        if (!victory) {
                            player = cells[player.x][player.y - 1]!!
                        }
                    }
                }
                Direction.DOWN -> {
                    if (!player.bottomWall) {
                        victory = checkVictory(player.y + 1, ROW)
                        if (!victory) {
                            player = cells[player.x][player.y + 1]!!
                        }
                    }
                }
                Direction.LEFT -> {
                    if (!player.leftWall) {
                        victory = checkVictory(player.x - 1, COLS)
                        if (!victory) {
                            player = cells[player.x - 1][player.y]!!
                        }
                    }
                }
                Direction.RIGHT -> {
                    if (!player.rightWall) {
                        victory = checkVictory(player.x + 1, COLS)
                        if (!victory) {
                            player = cells[player.x + 1][player.y]!!
                        }
                    }
                }
            }
        }
        invalidate()
        return victory
    }

    private fun checkVictory(indexNext : Int, limit : Int) : Boolean{
        var victory = false
        if (indexNext < 0 || indexNext >= limit){
            victory = true
            changeMaze()
        }
        return victory
    }

    private fun changeMaze(){
        if (Mode.mode != GameMode.EXTREME) {
            if (maze + 1 > 13) {
                Mode.mazeSelected.postValue(1)
            } else {
                Mode.mazeSelected.postValue(maze + 1)
            }
        }
        else{
            Mode.mazeSelected.postValue(maze + 1)
        }
    }

    private fun moveGlad(){
        playerCanMove = false
        coroutineScope.launch {
            for (i in 0..1){
                Thread.sleep(120)
                val xPlayer = player.x
                val yPlayer = player.y
                val xGlad = glad.x
                val yGlad = glad.y
                if (xGlad != xPlayer){
                    moveOnX(xPlayer,yPlayer,xGlad,yGlad)
                }else if (yGlad != yPlayer){
                    moveOnY(yPlayer,yGlad)
                }
                postInvalidate()
                val death = checkDeadOrMove(glad.x,glad.y)
                if (death) {
                    return@launch
                }
            }
            playerCanMove = true
        }
    }

    private fun moveOnX(
        xPlayer : Int,
        yPlayer: Int,
        xGlad : Int,
        yGlad : Int
    ){
        val xSign = (xPlayer - xGlad).sign
        if (xSign < 0 && !glad.leftWall) {
            glad = cells[glad.x-1][glad.y]!!
        } else if (xSign > 0 && !glad.rightWall) {
            glad = cells[glad.x+1][glad.y]!!
        }
        else{
            moveOnY(yPlayer,yGlad)
        }
    }

    private fun moveOnY(
    yPlayer: Int,
    yGlad : Int
    ){
        val ySign = (yPlayer - yGlad).sign
        if (ySign < 0 && !glad.topWall) {
            glad = cells[glad.x][glad.y-1]!!
        } else if (ySign > 0 && !glad.bottomWall) {
            glad = cells[glad.x][glad.y+1]!!
        }
    }

    private fun checkDeadOrMove(xNext : Int, yNext : Int) : Boolean{
        var dead = false
        if (xNext == player.x && yNext == player.y){
            resetMaze()
            dead = true
            playerCanMove = true
        }
        return dead
    }

    private fun resetMaze(){
        when(Mode.mode){
            GameMode.HISTORY -> Mode.mazeSelected.postValue(1)
            GameMode.ARCADE -> {Mode.mazeSelected.postValue(maze)}
            GameMode.EXTREME -> {Mode.mazeSelected.postValue(1)}
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawColor(context.getColor(R.color.background))

        val width = width
        val height = height

        val wDh : Float = width.toFloat()/height.toFloat()
        val cDr = COLS.toFloat()/ROW.toFloat()

        cellSize = if(wDh < cDr){
            width/(COLS+1)
        } else{
            height/(ROW+1)
        }

        hMargin = (width-COLS*cellSize)/2
        vMargin = (height-ROW*cellSize)/2

        canvas?.translate(hMargin.toFloat(),vMargin.toFloat())

        for (x  in 0 until COLS) {
            for (y in 0 until ROW) {
                 if(cells[x][y]!!.topWall){
                     canvas?.drawLine(
                         x*cellSize.toFloat(),
                         y*cellSize.toFloat(),
                         (x+1)*cellSize.toFloat(),
                         y*cellSize.toFloat(),
                         wallPaint
                     )
                 }

                if(cells[x][y]!!.leftWall){
                    //wallPaint.color = Color.RED
                    canvas?.drawLine(
                        x*cellSize.toFloat(),
                        y*cellSize.toFloat(),
                        x*cellSize.toFloat(),
                        (y+1)*cellSize.toFloat(),
                        wallPaint
                    )
                }

                if(cells[x][y]!!.rightWall){
                    //wallPaint.color = Color.BLACK
                    canvas?.drawLine(
                        (x+1)*cellSize.toFloat(),
                        y*cellSize.toFloat(),
                        (x+1)*cellSize.toFloat(),
                        (y+1)*cellSize.toFloat(),
                        wallPaint
                    )
                }

                if(cells[x][y]!!.bottomWall){
                    //wallPaint.color = Color.YELLOW
                    canvas?.drawLine(
                        x*cellSize.toFloat(),
                        (y+1)*cellSize.toFloat(),
                        (x+1)*cellSize.toFloat(),
                        (y+1)*cellSize.toFloat(),
                        wallPaint
                    )
                }
            }
        }

        var margin = cellSize/8

        val drawableGlad = resources.getDrawable(R.drawable.ic_glad,null)
        drawableGlad.setBounds(glad.x*cellSize+margin,
            glad.y*cellSize+margin,
            (glad.x+1)*cellSize-margin,
            (glad.y+1)*cellSize-margin
        )
        if (canvas != null) {
            drawableGlad.draw(canvas)
        }

        val drawablePlayer = resources.getDrawable(R.drawable.ic_player,null)
        drawablePlayer.setBounds(player.x*cellSize+margin,
            player.y*cellSize+margin,
            (player.x+1)*cellSize-margin,
            (player.y+1)*cellSize-margin
        )
        if (canvas != null) {
            drawablePlayer.draw(canvas)
        }

        textPaint.color = Color.BLACK
        textPaint.textSize = 40f
        textPaint.typeface = ResourcesCompat.getFont(context, R.font.montserrat_black);
        canvas?.drawText("Maze : $maze", 0F, -50F, textPaint)
    }

    private class Cell(var x : Int, var y : Int) {
        var topWall = false
        var leftWall = false
        var rightWall = false
        var bottomWall = false
        var visited = false
    }
    //______________________________________________________________________________________
    private fun createMazeRandom(){
        var nextPosition : Cell?
        COLS = (7 until 15).random()
        ROW = (7 until 15).random()

        initMaze()

        player = cells[COLS/2][0]!!
        glad  = cells[COLS/2][ROW-1]!!

        for (x in 0 until COLS) {
            for (y in 0 until ROW) {
                cells[x][y]?.topWall = true
                cells[x][y]?.bottomWall = true
                cells[x][y]?.leftWall = true
                cells[x][y]?.rightWall = true
                cells[x][y]?.visited = false
            }
        }

        var curentPosition : Cell? = player
        var isDone = false
        curentPosition?.visited = true
        do {

            nextPosition = curentPosition?.let {getNeighbour(it)}
            if (nextPosition != null && curentPosition != null){
                curentPosition = nextPosition
                curentPosition.visited = true
            }
            else{
                val randomCell = getNotVisitedCell()
                if (randomCell != null){
                    curentPosition = randomCell
                    curentPosition.visited = true
                }else{
                    isDone = true
                }
            }
        }while (!isDone)
        cells[COLS/2][ROW-1]?.bottomWall = false
    }

    private fun getNotVisitedCell() : Cell?{
        for (x in 0 until COLS) {
            for (y in 0 until ROW) {
                val cell = cells[x][y]
                if (cell != null && !cell.visited){
                    return cell
                }
            }
        }
        return null
    }

    private fun getNeighbour(current : Cell) : Cell? {
        var neighbours = ArrayList<Cell>()
        var neighboursVisited = ArrayList<Cell>()
        var random = Random()
        //LEFT
        if (current.x > 0){
            var cell = cells[current.x-1][current.y]
            if(cell != null){
                if (!cell.visited){
                    neighbours.add(cell)
                }
                else{
                    neighboursVisited.add(cell)
                }
            }
        }
        //RIGHT
        if (current.x < COLS-1){
            var cell = cells[current.x+1][current.y]
            if(cell != null){
                if (!cell.visited){
                    neighbours.add(cell)
                }
                else{
                    neighboursVisited.add(cell)
                }
            }
        }
        //TOP
        if (current.y > 0){
            var cell = cells[current.x][current.y-1]
            if(cell != null){
                if (!cell.visited){
                    neighbours.add(cell)
                }
                else{
                    neighboursVisited.add(cell)
                }
            }
        }
        //BOTTOM
        if (current.y < ROW-1){
            var cell = cells[current.x][current.y+1]
            if(cell != null){
                if (!cell.visited){
                    neighbours.add(cell)
                }
                else{
                    neighboursVisited.add(cell)
                }
            }
        }
        if (neighbours.size > 0){
            var index = 0
            for (i in 0..1){
                index = random.nextInt(neighbours.size)
                removeWall(current,neighbours[index])
            }
            return neighbours[index]
        }
        else if (neighboursVisited.size > 0){
            val index = random.nextInt(neighboursVisited.size)
            removeWall(current,neighboursVisited[index])
            return null
        }
        else{
            return null
        }
    }

    private fun removeWall(current : Cell, next : Cell){
        if (current.x == next.x && current.y == next.y+1){
            current.topWall = false
            next.bottomWall = false
        }
        else if (current.x == next.x && current.y == next.y-1){
            next.topWall = false
            current.bottomWall = false
        }
        else if (current.x == next.x+1 && current.y == next.y){
            current.leftWall = false
            next.rightWall = false
        }
        else if (current.x == next.x-1 && current.y == next.y){
            current.rightWall = false
            next.leftWall = false
        }
    }

    private fun initMaze(){
        cells = Array(COLS) { arrayOfNulls<Cell>(ROW) }
        for (x in 0 until COLS) {
            for (y in 0 until ROW) {
                cells[x][y] = Cell(x,y)
            }
        }
    }
    //______________________________________________________________________________________
    private fun createMaze1(){
        COLS = 3
        ROW = 3

        initMaze()

        //[COL][ROW]
        player = cells[1][2]!!
        glad  = cells[1][0]!!

        cells[0][0]?.topWall = true
        cells[0][0]?.leftWall = true

        cells[1][0]?.topWall = true
        cells[1][0]?.bottomWall = true

        cells[2][0]?.rightWall = true
        cells[2][0]?.topWall = true

        cells[0][1]?.leftWall = true

        cells[1][1]?.rightWall = true
        cells[1][1]?.topWall = true
        cells[1][1]?.bottomWall = true

        cells[2][1]?.leftWall = true

        cells[0][2]?.bottomWall = true
        cells[0][2]?.leftWall = true

        cells[1][2]?.topWall = true
        cells[1][2]?.bottomWall = true

        cells[2][2]?.bottomWall = true
        cells[2][2]?.rightWall = true
    }
    //______________________________________________________________________________________
    private fun createMaze2(){
        COLS = 7
        ROW = 4

        initMaze()

        //[COL][ROW]
        player = cells[2][1]!!
        glad  = cells[0][1]!!

        //1ere ligne
        cells[0][0]?.topWall = true
        cells[0][0]?.leftWall = true

        cells[1][0]?.topWall = true

        cells[2][0]?.topWall = true

        cells[3][0]?.topWall = true

        cells[4][0]?.topWall = true

        cells[5][0]?.topWall = true

        cells[6][0]?.topWall = true
        cells[6][0]?.rightWall = true

        //2eme ligne
        cells[0][1]?.leftWall = true
        cells[0][1]?.rightWall = true

        cells[1][1]?.bottomWall = true
        cells[1][1]?.leftWall = true
        cells[1][1]?.rightWall = true

        cells[2][1]?.leftWall = true

        cells[6][1]?.rightWall = true

        //3eme ligne
        cells[0][2]?.leftWall = true

        cells[1][2]?.topWall = true

        cells[4][2]?.rightWall = true

        cells[5][2]?.leftWall = true
        cells[5][2]?.bottomWall = true
        cells[5][2]?.rightWall = true

        cells[6][2]?.rightWall = true
        cells[6][2]?.leftWall = true

        //4eme ligne
        cells[0][3]?.leftWall = true
        cells[0][3]?.bottomWall = true

        cells[2][3]?.bottomWall = true

        cells[3][3]?.bottomWall = true

        cells[4][3]?.bottomWall = true

        cells[5][3]?.bottomWall = true
        cells[5][3]?.topWall = true

        cells[6][3]?.bottomWall = true
        cells[6][3]?.rightWall = true
    }
    //______________________________________________________________________________________
    private fun createMaze3(){
        COLS = 3
        ROW = 4

        initMaze()

        //[COL][ROW]
        player = cells[1][1]!!
        glad  = cells[1][0]!!

        //1ere ligne
        cells[0][0]?.topWall = true
        cells[0][0]?.leftWall = true

        cells[1][0]?.topWall = true
        cells[1][0]?.bottomWall = true

        cells[2][0]?.topWall = true
        cells[2][0]?.rightWall = true

        //2eme ligne
        cells[0][1]?.leftWall = true

        cells[1][1]?.topWall = true

        //3eme ligne
        cells[0][2]?.leftWall = true
        cells[0][2]?.rightWall = true
        cells[0][2]?.bottomWall = true

        cells[1][2]?.leftWall = true

        cells[2][2]?.rightWall = true

        //4eme ligne
        cells[0][3]?.leftWall = true
        cells[0][3]?.topWall = true
        cells[0][3]?.bottomWall = true

        cells[1][3]?.bottomWall = true

        cells[2][3]?.bottomWall = true
        cells[2][3]?.rightWall = true
    }
    //______________________________________________________________________________________
    private fun createMaze4(){
        COLS = 5
        ROW = 5

        initMaze()

        //[COL][ROW]
        player = cells[1][0]!!
        glad  = cells[4][1]!!

        //1er ligne
        cells[0][0]?.topWall = true
        cells[0][0]?.leftWall = true
        cells[0][0]?.rightWall = true

        cells[1][0]?.topWall = true
        cells[1][0]?.leftWall = true

        cells[2][0]?.topWall = true

        cells[3][0]?.bottomWall = true

        cells[4][0]?.topWall = true
        cells[4][0]?.rightWall = true

        //2eme ligne
        cells[0][1]?.leftWall = true

        cells[1][1]?.rightWall = true
        cells[1][1]?.bottomWall = true

        cells[2][1]?.leftWall = true

        cells[3][1]?.topWall = true
        cells[3][1]?.rightWall = true

        cells[4][1]?.leftWall = true
        cells[4][1]?.rightWall = true

        //3eme ligne
        cells[0][2]?.leftWall = true

        cells[1][2]?.topWall = true
        cells[1][2]?.bottomWall = true

        cells[2][2]?.bottomWall = true
        cells[2][2]?.rightWall = true

        cells[3][2]?.leftWall = true

        cells[4][2]?.rightWall = true

        //4eme ligne
        cells[0][3]?.leftWall = true

        cells[1][3]?.topWall = true

        cells[2][3]?.topWall = true
        cells[2][3]?.rightWall = true

        cells[3][3]?.bottomWall = true
        cells[3][3]?.rightWall = true
        cells[3][3]?.leftWall = true

        cells[4][3]?.leftWall = true
        cells[4][3]?.rightWall = true

        //5eme ligne
        cells[0][4]?.leftWall = true
        cells[0][4]?.bottomWall = true

        cells[1][4]?.bottomWall = true

        cells[2][4]?.bottomWall = true

        cells[3][4]?.bottomWall = true
        cells[3][4]?.topWall = true

        cells[4][4]?.bottomWall = true
        cells[4][4]?.rightWall = true
    }
    //______________________________________________________________________________________
    private fun createMaze5(){
        COLS = 7
        ROW = 5

        initMaze()

        //[COL][ROW]
        player = cells[2][1]!!
        glad  = cells[2][3]!!

        //1er ligne
        cells[0][0]?.topWall = true
        cells[0][0]?.leftWall = true

        cells[1][0]?.topWall = true

        cells[2][0]?.topWall = true

        cells[3][0]?.topWall = true
        cells[3][0]?.rightWall = true

        cells[4][0]?.topWall = true
        cells[4][0]?.leftWall = true

        cells[5][0]?.bottomWall = true
        cells[5][0]?.rightWall = true

        cells[6][0]?.topWall = true
        cells[6][0]?.leftWall = true
        cells[6][0]?.rightWall = true

        //2eme ligne
        cells[0][1]?.leftWall = true
        cells[0][1]?.rightWall = true

        cells[1][1]?.leftWall = true
        cells[1][1]?.bottomWall = true

        cells[3][1]?.rightWall = true

        cells[4][1]?.leftWall = true

        cells[5][1]?.bottomWall = true
        cells[5][1]?.topWall = true

        cells[6][1]?.rightWall = true

        //3eme ligne
        cells[0][2]?.leftWall = true

        cells[1][2]?.topWall = true
        cells[1][2]?.bottomWall = true

        cells[2][2]?.bottomWall = true
        cells[2][2]?.rightWall = true

        cells[3][2]?.leftWall = true

        cells[5][2]?.bottomWall = true
        cells[5][2]?.rightWall = true
        cells[5][2]?.topWall = true

        cells[6][2]?.leftWall = true
        cells[6][2]?.rightWall = true

        //4eme ligne
        cells[0][3]?.leftWall = true

        cells[1][3]?.topWall = true
        cells[1][3]?.rightWall = true

        cells[2][3]?.topWall = true
        cells[2][3]?.leftWall = true
        cells[2][3]?.bottomWall = true

        cells[3][3]?.bottomWall = true
        cells[3][3]?.rightWall = true

        cells[4][3]?.leftWall = true
        cells[4][3]?.rightWall = true
        cells[4][3]?.bottomWall = true

        cells[5][3]?.topWall = true
        cells[5][3]?.leftWall = true

        cells[6][3]?.rightWall = true

        //5eme ligne
        cells[0][4]?.leftWall = true
        cells[0][4]?.bottomWall = true

        cells[1][4]?.bottomWall = true

        cells[2][4]?.bottomWall = true
        cells[2][4]?.topWall = true

        cells[3][4]?.bottomWall = true
        cells[3][4]?.topWall = true

        cells[4][4]?.bottomWall = true
        cells[4][4]?.topWall = true

        cells[5][4]?.bottomWall = true

        cells[6][4]?.bottomWall = true
        cells[6][4]?.rightWall = true
    }
    //______________________________________________________________________________________
    private fun createMaze6(){
        COLS = 6
        ROW = 6

        initMaze()

        //[COL][ROW]
        player = cells[0][0]!!
        glad  = cells[4][2]!!

        //1er ligne
        cells[0][0]?.topWall = true
        cells[0][0]?.leftWall = true
        cells[0][0]?.bottomWall = true

        cells[1][0]?.topWall = true

        cells[2][0]?.topWall = true

        cells[3][0]?.topWall = true

        cells[4][0]?.rightWall = true

        cells[5][0]?.topWall = true
        cells[5][0]?.rightWall = true
        cells[5][0]?.leftWall = true

        //2eme ligne
        cells[0][1]?.topWall = true
        cells[0][1]?.leftWall = true

        cells[1][1]?.bottomWall = true
        cells[1][1]?.rightWall = true

        cells[2][1]?.leftWall = true

        cells[3][1]?.rightWall = true

        cells[4][1]?.leftWall = true
        cells[4][1]?.bottomWall = true
        cells[4][1]?.rightWall = true

        cells[5][1]?.leftWall = true
        cells[5][1]?.rightWall = true

        //3eme ligne
        cells[0][2]?.leftWall = true
        cells[0][2]?.rightWall = true

        cells[1][2]?.leftWall = true
        cells[1][2]?.topWall = true
        cells[1][2]?.rightWall = true

        cells[2][2]?.bottomWall = true
        cells[2][2]?.rightWall = true
        cells[2][2]?.leftWall = true

        cells[3][2]?.leftWall = true

        cells[4][2]?.bottomWall = true
        cells[4][2]?.topWall = true

        cells[5][2]?.rightWall = true

        //4eme ligne
        cells[0][3]?.leftWall = true
        cells[0][3]?.rightWall = true

        cells[1][3]?.leftWall = true

        cells[2][3]?.topWall = true
        cells[2][3]?.bottomWall = true

        cells[3][3]?.rightWall = true

        cells[4][3]?.leftWall = true
        cells[4][3]?.topWall = true

        cells[5][3]?.rightWall = true

        //5eme ligne
        cells[0][4]?.leftWall = true
        cells[0][4]?.rightWall = true

        cells[1][4]?.leftWall = true
        cells[1][4]?.rightWall = true

        cells[2][4]?.leftWall = true
        cells[2][4]?.topWall = true

        cells[5][4]?.rightWall = true

        //6eme ligne
        cells[0][5]?.leftWall = true
        cells[0][5]?.bottomWall = true

        cells[1][5]?.bottomWall = true

        cells[2][5]?.bottomWall = true

        cells[3][5]?.bottomWall = true

        cells[4][5]?.bottomWall = true

        cells[5][5]?.bottomWall = true
        cells[5][5]?.rightWall = true
    }
    //______________________________________________________________________________________
    private fun createMaze7(){
        COLS = 6
        ROW = 6

        initMaze()

        //[COL][ROW]
        player = cells[4][4]!!
        glad  = cells[0][4]!!

        //1er ligne
        cells[0][0]?.topWall = true
        cells[0][0]?.leftWall = true

        cells[1][0]?.bottomWall = true

        cells[2][0]?.topWall = true

        cells[3][0]?.topWall = true

        cells[4][0]?.rightWall = true
        cells[4][0]?.topWall = true
        cells[4][0]?.bottomWall = true

        cells[5][0]?.topWall = true
        cells[5][0]?.rightWall = true
        cells[5][0]?.leftWall = true

        //2eme ligne
        cells[0][1]?.leftWall = true

        cells[1][1]?.topWall = true

        cells[2][1]?.rightWall = true

        cells[3][1]?.leftWall = true

        cells[4][1]?.topWall = true
        cells[4][1]?.bottomWall = true

        cells[5][1]?.rightWall = true

        //3eme ligne
        cells[0][2]?.leftWall = true

        cells[2][2]?.rightWall = true

        cells[3][2]?.leftWall = true

        cells[4][2]?.rightWall = true
        cells[4][2]?.topWall = true

        cells[5][2]?.rightWall = true
        cells[5][2]?.leftWall = true

        //4eme ligne
        cells[0][3]?.leftWall = true
        cells[0][3]?.rightWall = true
        cells[0][3]?.bottomWall = true

        cells[1][3]?.leftWall = true

        cells[3][3]?.rightWall = true

        cells[4][3]?.leftWall = true
        cells[4][3]?.rightWall = true
        cells[4][3]?.bottomWall = true

        cells[5][3]?.rightWall = true
        cells[5][3]?.leftWall = true

        //5eme ligne
        cells[0][4]?.leftWall = true
        cells[0][4]?.topWall = true

        cells[2][4]?.bottomWall = true

        cells[4][4]?.topWall = true
        cells[4][4]?.rightWall = true

        cells[5][4]?.rightWall = true
        cells[5][4]?.leftWall = true

        //6eme ligne
        cells[0][5]?.leftWall = true
        cells[0][5]?.bottomWall = true

        cells[1][5]?.bottomWall = true

        cells[2][5]?.bottomWall = true
        cells[2][5]?.topWall = true

        cells[3][5]?.bottomWall = true

        cells[4][5]?.bottomWall = true

        cells[5][5]?.bottomWall = true
        cells[5][5]?.rightWall = true
    }
    //______________________________________________________________________________________
    private fun createMaze8(){
        COLS = 9
        ROW = 8

        initMaze()

        //[COL][ROW]
        player = cells[0][0]!!
        glad  = cells[2][2]!!

        //1er ligne
        cells[0][0]?.topWall = true
        cells[0][0]?.leftWall = true
        cells[0][0]?.bottomWall = true

        cells[1][0]?.topWall = true
        cells[1][0]?.bottomWall = true

        cells[2][0]?.topWall = true

        cells[3][0]?.topWall = true
        cells[3][0]?.rightWall = true

        cells[4][0]?.topWall = true
        cells[4][0]?.leftWall = true

        cells[5][0]?.topWall = true
        cells[5][0]?.rightWall = true

        cells[6][0]?.topWall = true
        cells[6][0]?.leftWall = true

        cells[7][0]?.topWall = true
        cells[7][0]?.bottomWall = true

        cells[8][0]?.topWall = true
        cells[8][0]?.rightWall = true

        //2eme ligne
        cells[0][1]?.topWall = true
        cells[0][1]?.leftWall = true

        cells[1][1]?.topWall = true
        cells[1][1]?.rightWall = true

        cells[2][1]?.leftWall = true
        cells[2][1]?.bottomWall = true
        cells[2][1]?.rightWall = true

        cells[3][1]?.rightWall = true
        cells[3][1]?.leftWall = true

        cells[4][1]?.leftWall = true
        cells[4][1]?.rightWall = true

        cells[5][1]?.leftWall = true
        cells[5][1]?.rightWall = true

        cells[6][1]?.leftWall = true

        cells[7][1]?.topWall = true
        cells[7][1]?.bottomWall = true
        cells[7][1]?.rightWall = true

        cells[8][1]?.leftWall = true
        cells[8][1]?.rightWall = true

        //3eme ligne
        cells[0][2]?.leftWall = true
        cells[0][2]?.rightWall = true

        cells[1][2]?.leftWall = true
        cells[1][2]?.bottomWall = true

        cells[2][2]?.topWall = true
        cells[2][2]?.rightWall = true

        cells[3][2]?.leftWall = true

        cells[4][2]?.bottomWall = true
        cells[4][2]?.rightWall = true

        cells[5][2]?.bottomWall = true
        cells[5][2]?.leftWall = true

        cells[6][2]?.bottomWall = true
        cells[6][2]?.rightWall = true

        cells[7][2]?.topWall = true
        cells[7][2]?.leftWall = true

        cells[8][2]?.rightWall = true

        //4eme ligne
        cells[0][3]?.leftWall = true
        cells[0][3]?.bottomWall = true

        cells[1][3]?.topWall = true
        cells[1][3]?.rightWall = true

        cells[2][3]?.leftWall = true
        cells[2][3]?.rightWall = true

        cells[3][3]?.leftWall = true
        cells[3][3]?.bottomWall = true

        cells[4][3]?.rightWall = true
        cells[4][3]?.topWall = true

        cells[5][3]?.leftWall = true
        cells[5][3]?.topWall = true

        cells[6][3]?.bottomWall = true
        cells[6][3]?.topWall = true

        cells[7][3]?.bottomWall = true

        cells[8][3]?.rightWall = true
        cells[8][3]?.bottomWall = true

        //5eme ligne
        cells[0][4]?.leftWall = true
        cells[0][4]?.rightWall = true
        cells[0][4]?.topWall = true

        cells[1][4]?.leftWall = true
        cells[1][4]?.rightWall = true

        cells[2][4]?.leftWall = true
        cells[2][4]?.bottomWall = true

        cells[3][4]?.topWall = true

        cells[4][4]?.rightWall = true
        cells[4][4]?.bottomWall = true

        cells[5][4]?.leftWall = true
        cells[5][4]?.bottomWall = true

        cells[6][4]?.topWall = true
        cells[6][4]?.bottomWall = true

        cells[7][4]?.topWall = true
        cells[7][4]?.bottomWall = true

        cells[8][4]?.rightWall = true
        cells[8][4]?.topWall = true

        //6eme ligne
        cells[0][5]?.leftWall = true

        cells[1][5]?.bottomWall = true
        cells[1][5]?.rightWall = true

        cells[2][5]?.topWall = true
        cells[2][5]?.leftWall = true

        cells[3][5]?.bottomWall = true
        cells[3][5]?.rightWall = true

        cells[4][5]?.topWall = true
        cells[4][5]?.leftWall = true

        cells[5][5]?.topWall = true

        cells[6][5]?.topWall = true
        cells[6][5]?.bottomWall = true

        cells[7][5]?.topWall = true
        cells[7][5]?.bottomWall = true

        cells[8][5]?.rightWall = true
        cells[8][5]?.bottomWall = true

        //7eme ligne
        cells[0][6]?.leftWall = true
        cells[0][6]?.rightWall = true

        cells[1][6]?.topWall = true
        cells[1][6]?.rightWall = true
        cells[1][6]?.leftWall = true

        cells[2][6]?.leftWall = true

        cells[3][6]?.bottomWall = true
        cells[3][6]?.rightWall = true
        cells[3][6]?.topWall = true

        cells[4][6]?.rightWall = true
        cells[4][6]?.leftWall = true

        cells[5][6]?.leftWall = true
        cells[5][6]?.bottomWall = true

        cells[6][6]?.topWall = true
        cells[6][6]?.bottomWall = true

        cells[7][6]?.topWall = true
        cells[7][6]?.bottomWall = true

        cells[8][6]?.rightWall = true
        cells[8][6]?.topWall = true

        //8eme ligne
        cells[0][7]?.leftWall = true
        cells[0][7]?.rightWall = true
        cells[0][7]?.bottomWall = true

        cells[1][7]?.leftWall = true

        cells[2][7]?.bottomWall = true

        cells[3][7]?.bottomWall = true
        cells[3][7]?.topWall = true

        cells[4][7]?.bottomWall = true

        cells[5][7]?.rightWall = true
        cells[5][7]?.topWall = true
        cells[5][7]?.bottomWall = true

        cells[6][7]?.topWall = true
        cells[6][7]?.leftWall = true
        cells[6][7]?.bottomWall = true

        cells[7][7]?.topWall = true
        cells[7][7]?.bottomWall = true

        cells[8][7]?.rightWall = true
        cells[8][7]?.bottomWall = true
    }
    //______________________________________________________________________________________
    private fun createMaze9(){
        COLS = 9
        ROW = 8

        initMaze()

        //[COL][ROW]
        player = cells[8][0]!!
        glad  = cells[0][0]!!

        //1er ligne
        cells[0][0]?.topWall = true
        cells[0][0]?.leftWall = true
        cells[0][0]?.bottomWall = true

        cells[1][0]?.topWall = true
        cells[1][0]?.bottomWall = true

        cells[2][0]?.topWall = true

        cells[4][0]?.topWall = true

        cells[5][0]?.topWall = true
        cells[5][0]?.bottomWall = true

        cells[6][0]?.topWall = true

        cells[7][0]?.topWall = true

        cells[8][0]?.topWall = true
        cells[8][0]?.rightWall = true

        //2eme ligne
        cells[0][1]?.topWall = true
        cells[0][1]?.leftWall = true

        cells[1][1]?.topWall = true

        cells[5][1]?.topWall = true

        cells[8][1]?.rightWall = true

        //3eme ligne
        cells[0][2]?.leftWall = true

        cells[3][2]?.bottomWall = true

        cells[8][2]?.rightWall = true

        //4eme ligne
        cells[0][3]?.leftWall = true
        cells[0][3]?.rightWall = true

        cells[1][3]?.bottomWall = true
        cells[1][3]?.leftWall = true

        cells[2][3]?.rightWall = true

        cells[3][3]?.leftWall = true
        cells[3][3]?.topWall = true
        cells[3][3]?.rightWall = true

        cells[4][3]?.leftWall = true

        cells[7][3]?.rightWall = true

        cells[8][3]?.rightWall = true
        cells[8][3]?.leftWall = true

        //5eme ligne
        cells[0][4]?.leftWall = true
        cells[0][4]?.rightWall = true

        cells[1][4]?.leftWall = true
        cells[1][4]?.topWall = true

        cells[7][4]?.rightWall = true
        cells[7][4]?.bottomWall = true

        cells[8][4]?.rightWall = true
        cells[8][4]?.leftWall = true

        //6eme ligne
        cells[0][5]?.leftWall = true
        cells[0][5]?.rightWall = true

        cells[1][5]?.leftWall = true

        cells[7][5]?.topWall = true
        cells[7][5]?.rightWall = true

        cells[8][5]?.rightWall = true
        cells[8][5]?.leftWall = true

        //7eme ligne
        cells[0][6]?.leftWall = true

        cells[3][6]?.bottomWall = true

        cells[7][6]?.rightWall = true

        cells[8][6]?.rightWall = true
        cells[8][6]?.leftWall = true

        //8eme ligne
        cells[0][7]?.leftWall = true
        cells[0][7]?.bottomWall = true

        cells[1][7]?.bottomWall = true

        cells[2][7]?.bottomWall = true

        cells[3][7]?.bottomWall = true
        cells[3][7]?.topWall = true

        cells[4][7]?.bottomWall = true

        cells[5][7]?.bottomWall = true

        cells[6][7]?.bottomWall = true

        cells[7][7]?.bottomWall = true

        cells[8][7]?.rightWall = true
        cells[8][7]?.bottomWall = true
    }
    //______________________________________________________________________________________
    private fun createMaze10(){
        COLS = 8
        ROW = 8

        initMaze()

        //[COL][ROW]
        player = cells[1][1]!!
        glad  = cells[0][0]!!

        //1er ligne
        cells[0][0]?.topWall = true
        cells[0][0]?.rightWall = true

        cells[1][0]?.topWall = true
        cells[1][0]?.leftWall = true

        cells[2][0]?.topWall = true

        cells[3][0]?.topWall = true

        cells[4][0]?.topWall = true

        cells[5][0]?.topWall = true
        cells[5][0]?.bottomWall = true

        cells[6][0]?.topWall = true

        cells[7][0]?.topWall = true
        cells[7][0]?.rightWall = true
        cells[7][0]?.bottomWall = true

        //2eme ligne
        cells[0][1]?.leftWall = true

        cells[5][1]?.topWall = true

        cells[6][1]?.rightWall = true

        cells[7][1]?.topWall = true
        cells[7][1]?.leftWall = true
        cells[7][1]?.rightWall = true

        //3eme ligne
        cells[0][2]?.leftWall = true

        cells[4][2]?.rightWall = true

        cells[5][2]?.leftWall = true

        cells[7][2]?.rightWall = true

        //4eme ligne
        cells[0][3]?.leftWall = true

        cells[1][3]?.bottomWall = true

        cells[3][3]?.rightWall = true

        cells[4][3]?.leftWall = true

        cells[5][3]?.bottomWall = true

        cells[7][3]?.rightWall = true

        //5eme ligne
        cells[0][4]?.leftWall = true

        cells[1][4]?.topWall = true

        cells[2][4]?.rightWall = true

        cells[3][4]?.leftWall = true

        cells[5][4]?.topWall = true

        cells[7][4]?.rightWall = true

        //6eme ligne
        cells[0][5]?.leftWall = true
        cells[0][5]?.rightWall = true
        cells[0][5]?.bottomWall = true

        cells[1][5]?.leftWall = true
        cells[1][5]?.rightWall = true

        cells[2][5]?.leftWall = true

        cells[5][5]?.rightWall = true

        cells[6][5]?.leftWall = true
        cells[6][5]?.bottomWall = true

        cells[7][5]?.rightWall = true

        //7eme ligne
        cells[0][6]?.leftWall = true
        cells[0][6]?.topWall = true
        cells[0][6]?.bottomWall = true

        cells[1][6]?.rightWall = true

        cells[2][6]?.leftWall = true

        cells[3][6]?.rightWall = true

        cells[4][6]?.bottomWall = true
        cells[4][6]?.leftWall = true

        cells[6][6]?.topWall = true

        cells[7][6]?.rightWall = true

        //8eme ligne
        cells[0][7]?.leftWall = true
        cells[0][7]?.topWall = true
        cells[0][7]?.bottomWall = true

        cells[1][7]?.bottomWall = true

        cells[2][7]?.bottomWall = true

        cells[3][7]?.bottomWall = true

        cells[4][7]?.bottomWall = true
        cells[4][7]?.topWall = true
        cells[4][7]?.rightWall = true

        cells[5][7]?.leftWall = true
        cells[5][7]?.bottomWall = true

        cells[6][7]?.bottomWall = true

        cells[7][7]?.rightWall = true
        cells[7][7]?.bottomWall = true
    }
    //______________________________________________________________________________________
    private fun createMaze11(){
        COLS = 14
        ROW = 9

        initMaze()

        //[COL][ROW]
        player = cells[0][4]!!
        glad  = cells[13][4]!!

        //1er ligne
        cells[0][0]?.topWall = true
        cells[0][0]?.leftWall = true
        cells[0][0]?.bottomWall = true

        cells[1][0]?.topWall = true
        cells[1][0]?.bottomWall = true

        cells[2][0]?.topWall = true

        cells[3][0]?.topWall = true

        cells[4][0]?.topWall = true
        cells[4][0]?.rightWall = true

        cells[5][0]?.topWall = true
        cells[5][0]?.leftWall = true

        cells[6][0]?.topWall = true
        cells[6][0]?.bottomWall = true

        cells[7][0]?.topWall = true

        cells[8][0]?.topWall = true
        cells[8][0]?.bottomWall = true
        cells[8][0]?.rightWall = true

        cells[9][0]?.topWall = true
        cells[9][0]?.bottomWall = true
        cells[9][0]?.leftWall = true

        cells[10][0]?.topWall = true

        cells[11][0]?.topWall = true
        cells[11][0]?.bottomWall = true

        cells[12][0]?.topWall = true
        cells[12][0]?.bottomWall = true

        cells[13][0]?.topWall = true
        cells[13][0]?.rightWall = true

        //2eme ligne
        cells[0][1]?.topWall = true
        cells[0][1]?.leftWall = true
        cells[0][1]?.rightWall = true

        cells[1][1]?.topWall = true
        cells[1][1]?.leftWall = true
        cells[1][1]?.rightWall = true

        cells[2][1]?.leftWall = true
        cells[2][1]?.rightWall = true

        cells[3][1]?.rightWall = true
        cells[3][1]?.leftWall = true
        cells[3][1]?.bottomWall = true

        cells[4][1]?.leftWall = true
        cells[4][1]?.rightWall = true

        cells[5][1]?.leftWall = true
        cells[5][1]?.rightWall = true

        cells[6][1]?.leftWall = true
        cells[6][1]?.topWall = true
        cells[6][1]?.bottomWall = true

        cells[8][1]?.topWall = true
        cells[8][1]?.bottomWall = true

        cells[9][1]?.topWall = true
        cells[9][1]?.bottomWall = true

        cells[10][1]?.bottomWall = true
        cells[10][1]?.rightWall = true

        cells[11][1]?.topWall = true
        cells[11][1]?.bottomWall = true
        cells[11][1]?.leftWall = true

        cells[12][1]?.topWall = true
        cells[12][1]?.bottomWall = true

        //3eme ligne
        cells[0][2]?.leftWall = true

        cells[2][2]?.bottomWall = true

        cells[3][2]?.topWall = true

        cells[6][2]?.bottomWall = true
        cells[6][2]?.topWall = true

        cells[8][2]?.topWall = true

        cells[9][2]?.topWall = true

        cells[10][2]?.topWall = true
        cells[10][2]?.bottomWall = true

        cells[11][2]?.topWall = true
        cells[11][2]?.bottomWall = true

        cells[12][2]?.topWall = true

        cells[13][2]?.rightWall = true

        //4eme ligne
        cells[0][3]?.leftWall = true
        cells[0][3]?.rightWall = true

        cells[1][3]?.leftWall = true
        cells[1][3]?.rightWall = true

        cells[2][3]?.leftWall = true
        cells[2][3]?.bottomWall = true
        cells[2][3]?.topWall = true

        cells[3][3]?.rightWall = true

        cells[4][3]?.rightWall = true
        cells[4][3]?.leftWall = true

        cells[5][3]?.leftWall = true

        cells[6][3]?.topWall = true

        cells[7][3]?.rightWall = true

        cells[8][3]?.rightWall = true
        cells[8][3]?.leftWall = true

        cells[9][3]?.bottomWall = true
        cells[9][3]?.leftWall = true

        cells[10][3]?.bottomWall = true
        cells[10][3]?.topWall = true

        cells[11][3]?.topWall = true
        cells[11][3]?.bottomWall = true

        cells[12][3]?.rightWall = true

        cells[13][3]?.leftWall = true
        cells[13][3]?.rightWall = true

        //5eme ligne
        cells[0][4]?.leftWall = true
        cells[0][4]?.rightWall = true

        cells[1][4]?.leftWall = true
        cells[1][4]?.rightWall = true

        cells[2][4]?.leftWall = true
        cells[2][4]?.topWall = true

        cells[3][4]?.rightWall = true

        cells[4][4]?.rightWall = true
        cells[4][4]?.leftWall = true

        cells[5][4]?.leftWall = true
        cells[5][4]?.rightWall = true

        cells[6][4]?.rightWall = true
        cells[6][4]?.leftWall = true

        cells[7][4]?.rightWall = true
        cells[7][4]?.leftWall = true

        cells[8][4]?.leftWall = true

        cells[9][4]?.topWall = true
        cells[9][4]?.bottomWall = true

        cells[10][4]?.topWall = true
        cells[10][4]?.bottomWall = true

        cells[11][4]?.topWall = true
        cells[11][4]?.rightWall = true

        cells[12][4]?.leftWall = true
        cells[12][4]?.rightWall = true

        cells[13][4]?.leftWall = true
        cells[13][4]?.rightWall = true

        //6eme ligne
        cells[0][5]?.leftWall = true
        cells[0][5]?.rightWall = true

        cells[1][5]?.leftWall = true
        cells[1][5]?.rightWall = true

        cells[2][5]?.rightWall = true
        cells[2][5]?.leftWall = true

        cells[3][5]?.leftWall = true

        cells[4][5]?.rightWall = true

        cells[5][5]?.rightWall = true
        cells[5][5]?.leftWall = true

        cells[6][5]?.leftWall = true

        cells[9][5]?.topWall = true
        cells[9][5]?.bottomWall = true

        cells[10][5]?.topWall = true
        cells[10][5]?.bottomWall = true

        cells[11][5]?.rightWall = true

        cells[12][5]?.rightWall = true
        cells[12][5]?.leftWall = true

        cells[13][5]?.rightWall = true
        cells[13][5]?.leftWall = true

        //7eme ligne
        cells[0][6]?.leftWall = true
        cells[0][6]?.rightWall = true

        cells[1][6]?.rightWall = true
        cells[1][6]?.leftWall = true

        cells[2][6]?.leftWall = true
        cells[2][6]?.rightWall = true

        cells[3][6]?.leftWall = true
        cells[3][6]?.rightWall = true

        cells[4][6]?.rightWall = true
        cells[4][6]?.leftWall = true

        cells[5][6]?.leftWall = true
        cells[5][6]?.rightWall = true

        cells[6][6]?.leftWall = true
        cells[6][6]?.rightWall = true

        cells[7][6]?.leftWall = true

        cells[9][6]?.topWall = true
        cells[9][6]?.bottomWall = true

        cells[10][6]?.topWall = true
        cells[10][6]?.bottomWall = true

        cells[11][6]?.rightWall = true

        cells[12][6]?.rightWall = true
        cells[12][6]?.leftWall = true

        cells[13][6]?.rightWall = true
        cells[13][6]?.leftWall = true

        //7eme ligne
        cells[0][7]?.leftWall = true
        cells[0][7]?.rightWall = true

        cells[1][7]?.rightWall = true
        cells[1][7]?.leftWall = true

        cells[2][7]?.leftWall = true
        cells[2][7]?.rightWall = true

        cells[3][7]?.leftWall = true
        cells[3][7]?.rightWall = true

        cells[4][7]?.rightWall = true
        cells[4][7]?.leftWall = true

        cells[5][7]?.leftWall = true
        cells[5][7]?.rightWall = true

        cells[6][7]?.leftWall = true
        cells[6][7]?.rightWall = true

        cells[7][7]?.leftWall = true

        cells[9][7]?.topWall = true
        cells[9][7]?.bottomWall = true

        cells[10][7]?.topWall = true
        cells[10][7]?.bottomWall = true

        cells[11][7]?.rightWall = true

        cells[12][7]?.rightWall = true
        cells[12][7]?.leftWall = true

        cells[13][7]?.rightWall = true
        cells[13][7]?.leftWall = true

        //8eme ligne
        cells[0][8]?.leftWall = true
        cells[0][8]?.bottomWall = true

        cells[1][8]?.bottomWall = true

        cells[2][8]?.bottomWall = true

        cells[3][8]?.bottomWall = true

        cells[4][8]?.bottomWall = true

        cells[5][8]?.bottomWall = true

        cells[6][8]?.bottomWall = true

        cells[7][8]?.bottomWall = true

        cells[8][8]?.bottomWall = true

        cells[9][8]?.topWall = true
        cells[9][8]?.bottomWall = true

        cells[10][8]?.topWall = true
        cells[10][8]?.bottomWall = true

        cells[11][8]?.bottomWall = true

        cells[12][8]?.bottomWall = true

        cells[13][8]?.rightWall = true
        cells[13][8]?.bottomWall = true
    }
    //______________________________________________________________________________________
    private fun createMaze12(){
        COLS = 14
        ROW = 9

        initMaze()

        //[COL][ROW]
        player = cells[6][3]!!
        glad  = cells[12][5]!!

        //1er ligne
        cells[0][0]?.topWall = true
        cells[0][0]?.leftWall = true

        cells[1][0]?.topWall = true
        cells[1][0]?.rightWall = true

        cells[2][0]?.leftWall = true
        cells[2][0]?.topWall = true

        cells[3][0]?.topWall = true
        cells[3][0]?.bottomWall = true

        cells[4][0]?.topWall = true

        cells[5][0]?.topWall = true
        cells[5][0]?.bottomWall = true

        cells[6][0]?.topWall = true
        cells[6][0]?.bottomWall = true

        cells[7][0]?.topWall = true

        cells[8][0]?.topWall = true

        cells[9][0]?.topWall = true
        cells[9][0]?.bottomWall = true
        cells[9][0]?.rightWall = true

        cells[10][0]?.topWall = true
        cells[10][0]?.leftWall = true

        cells[11][0]?.topWall = true
        cells[11][0]?.bottomWall = true

        cells[12][0]?.topWall = true
        cells[12][0]?.bottomWall = true

        cells[13][0]?.topWall = true
        cells[13][0]?.rightWall = true

        //2eme ligne
        cells[0][1]?.leftWall = true
        cells[0][1]?.rightWall = true

        cells[1][1]?.leftWall = true
        cells[1][1]?.bottomWall = true

        cells[2][1]?.bottomWall = true

        cells[3][1]?.rightWall = true
        cells[3][1]?.topWall = true

        cells[4][1]?.leftWall = true
        cells[4][1]?.rightWall = true

        cells[5][1]?.leftWall = true
        cells[5][1]?.topWall = true

        cells[6][1]?.topWall = true
        cells[6][1]?.bottomWall = true

        cells[7][1]?.rightWall = true

        cells[8][1]?.leftWall = true

        cells[9][1]?.bottomWall = true
        cells[9][1]?.rightWall = true
        cells[9][1]?.topWall = true

        cells[10][1]?.rightWall = true
        cells[10][1]?.leftWall = true

        cells[11][1]?.topWall = true
        cells[11][1]?.rightWall = true
        cells[11][1]?.leftWall = true

        cells[12][1]?.topWall = true
        cells[12][1]?.rightWall = true
        cells[12][1]?.leftWall = true

        cells[13][1]?.leftWall = true
        cells[13][1]?.rightWall = true

        //3eme ligne
        cells[0][2]?.leftWall = true

        cells[1][2]?.topWall = true
        cells[1][2]?.rightWall = true

        cells[2][2]?.topWall = true
        cells[2][2]?.leftWall = true

        cells[3][2]?.bottomWall = true

        cells[4][2]?.bottomWall = true

        cells[5][2]?.bottomWall = true

        cells[6][2]?.topWall = true
        cells[6][2]?.bottomWall = true

        cells[8][2]?.bottomWall = true

        cells[9][2]?.topWall = true
        cells[9][2]?.rightWall = true

        cells[10][2]?.rightWall = true
        cells[10][2]?.leftWall = true

        cells[11][2]?.leftWall = true

        cells[13][2]?.rightWall = true

        //4eme ligne
        cells[0][3]?.leftWall = true
        cells[0][3]?.rightWall = true

        cells[1][3]?.leftWall = true

        cells[3][3]?.topWall = true

        cells[4][3]?.rightWall = true
        cells[4][3]?.topWall = true

        cells[5][3]?.leftWall = true
        cells[5][3]?.rightWall = true
        cells[5][3]?.topWall = true

        cells[6][3]?.topWall = true
        cells[6][3]?.bottomWall = true
        cells[6][3]?.leftWall = true

        cells[8][3]?.rightWall = true
        cells[8][3]?.topWall = true

        cells[9][3]?.leftWall = true

        cells[10][3]?.rightWall = true

        cells[11][3]?.rightWall = true
        cells[11][3]?.leftWall = true

        cells[12][3]?.rightWall = true
        cells[12][3]?.leftWall = true

        cells[13][3]?.leftWall = true
        cells[13][3]?.rightWall = true

        //5eme ligne
        cells[0][4]?.leftWall = true
        cells[0][4]?.rightWall = true

        cells[1][4]?.leftWall = true
        cells[1][4]?.rightWall = true

        cells[2][4]?.leftWall = true
        cells[2][4]?.rightWall = true

        cells[3][4]?.rightWall = true
        cells[3][4]?.leftWall = true

        cells[4][4]?.rightWall = true
        cells[4][4]?.leftWall = true

        cells[5][4]?.leftWall = true

        cells[6][4]?.rightWall = true
        cells[6][4]?.topWall = true

        cells[7][4]?.rightWall = true
        cells[7][4]?.leftWall = true

        cells[8][4]?.leftWall = true
        cells[8][4]?.rightWall = true

        cells[9][4]?.leftWall = true
        cells[9][4]?.rightWall = true

        cells[10][4]?.leftWall = true

        cells[12][4]?.bottomWall = true

        cells[13][4]?.bottomWall = true
        cells[13][4]?.rightWall = true

        //6eme ligne
        cells[0][5]?.leftWall = true

        cells[1][5]?.bottomWall = true

        cells[2][5]?.bottomWall = true

        cells[3][5]?.bottomWall = true

        cells[4][5]?.rightWall = true

        cells[5][5]?.rightWall = true
        cells[5][5]?.leftWall = true

        cells[6][5]?.leftWall = true

        cells[7][5]?.rightWall = true

        cells[8][5]?.leftWall = true
        cells[8][5]?.rightWall = true

        cells[9][5]?.rightWall = true
        cells[9][5]?.leftWall = true

        cells[10][5]?.leftWall = true
        cells[10][5]?.rightWall = true

        cells[11][5]?.rightWall = true
        cells[11][5]?.leftWall = true

        cells[12][5]?.rightWall = true
        cells[12][5]?.topWall = true
        cells[12][5]?.leftWall = true

        cells[13][5]?.topWall = true
        cells[13][5]?.leftWall = true

        //7eme ligne
        cells[0][6]?.leftWall = true
        cells[0][6]?.bottomWall = true

        cells[1][6]?.topWall = true
        cells[1][6]?.bottomWall = true

        cells[2][6]?.topWall = true

        cells[3][6]?.topWall = true
        cells[3][6]?.rightWall = true

        cells[4][6]?.bottomWall = true
        cells[4][6]?.leftWall = true

        cells[6][6]?.rightWall = true

        cells[7][6]?.leftWall = true

        cells[8][6]?.bottomWall = true

        cells[9][6]?.rightWall = true

        cells[10][6]?.leftWall = true
        cells[10][6]?.bottomWall = true

        cells[12][6]?.rightWall = true
        cells[12][6]?.bottomWall = true

        cells[13][6]?.rightWall = true
        cells[13][6]?.leftWall = true

        //8eme ligne
        cells[0][7]?.leftWall = true
        cells[0][7]?.rightWall = true
        cells[0][7]?.topWall = true

        cells[1][7]?.leftWall = true
        cells[1][7]?.rightWall = true
        cells[1][7]?.topWall = true

        cells[2][7]?.leftWall = true
        cells[2][7]?.rightWall = true

        cells[3][7]?.leftWall = true
        cells[3][7]?.rightWall = true

        cells[4][7]?.leftWall = true
        cells[4][7]?.topWall = true
        cells[4][7]?.bottomWall = true

        cells[5][7]?.bottomWall = true

        cells[6][7]?.rightWall = true

        cells[7][7]?.leftWall = true

        cells[8][7]?.topWall = true

        cells[9][7]?.bottomWall = true

        cells[10][7]?.topWall = true
        cells[10][7]?.bottomWall = true

        cells[11][7]?.bottomWall = true

        cells[12][7]?.topWall = true
        cells[12][7]?.rightWall = true

        cells[13][7]?.rightWall = true
        cells[13][7]?.leftWall = true

        //9eme ligne
        cells[0][8]?.leftWall = true
        cells[0][8]?.bottomWall = true

        cells[1][8]?.bottomWall = true

        cells[2][8]?.bottomWall = true

        cells[3][8]?.bottomWall = true

        cells[4][8]?.topWall = true
        cells[4][8]?.bottomWall = true

        cells[5][8]?.bottomWall = true
        cells[5][8]?.topWall = true

        cells[6][8]?.bottomWall = true

        cells[7][8]?.rightWall = true
        cells[7][8]?.bottomWall = true

        cells[8][8]?.bottomWall = true
        cells[8][8]?.leftWall = true

        cells[9][8]?.bottomWall = true
        cells[9][8]?.topWall = true

        cells[10][8]?.topWall = true
        cells[10][8]?.bottomWall = true

        cells[11][8]?.bottomWall = true
        cells[11][8]?.topWall = true

        cells[12][8]?.bottomWall = true

        cells[13][8]?.rightWall = true
        cells[13][8]?.bottomWall = true
    }
    //______________________________________________________________________________________
    private fun createMazeBonus(){
        COLS = 7
        ROW = 6

        initMaze()

        for (x in 0 until COLS) {
            for (y in 0 until ROW) {
                cells[x][y]?.topWall = true
                cells[x][y]?.bottomWall = true
                cells[x][y]?.leftWall = true
                cells[x][y]?.rightWall = true
            }
        }

        //[COL][ROW]
        player = cells[3][3]!!
        glad  = cells[0][5]!!

        //1er ligne
        cells[0][0]?.bottomWall = false

        cells[1][0]?.bottomWall = false
        cells[1][0]?.leftWall = false

        cells[2][0]?.leftWall = false

        cells[3][0]?.leftWall = false

        cells[4][0]?.leftWall = false

        cells[5][0]?.leftWall = false

        cells[6][0]?.leftWall = false

        //2eme ligne
        cells[0][1]?.bottomWall = false
        cells[0][1]?.rightWall = false

        cells[1][1]?.rightWall = false

        cells[2][1]?.bottomWall = false
        cells[2][1]?.rightWall = false

        cells[3][1]?.rightWall = false

        cells[4][1]?.bottomWall = false

        cells[5][1]?.rightWall = false

        cells[6][1]?.topWall = false

        //3eme ligne
        cells[0][2]?.bottomWall = false

        cells[1][2]?.topWall = false

        cells[2][2]?.bottomWall = false
        cells[2][2]?.leftWall = false

        cells[3][2]?.leftWall = false

        cells[4][2]?.bottomWall = false

        cells[5][2]?.topWall = false

        //4eme ligne
        cells[0][3]?.bottomWall = false

        cells[1][3]?.leftWall = false

        cells[2][3]?.leftWall = false
        cells[2][3]?.rightWall = false

        cells[3][3]?.topWall = false
        cells[3][3]?.bottomWall = false

        cells[4][3]?.rightWall = false

        cells[5][3]?.bottomWall = false
        cells[5][3]?.topWall = false

        cells[6][3]?.leftWall = false

        //5eme ligne
        cells[0][4]?.bottomWall = false

        cells[2][4]?.topWall = false

        cells[3][4]?.bottomWall = false

        cells[4][4]?.leftWall = false

        cells[5][4]?.leftWall = false

        cells[6][4]?.topWall = false
        //6eme ligne
        cells[0][5]?.leftWall = false
        cells[0][5]?.rightWall = false

        cells[1][5]?.rightWall = false

        cells[2][5]?.topWall = false

        cells[3][5]?.rightWall = false

        cells[4][5]?.rightWall = false

        cells[5][5]?.rightWall = false

        cells[6][5]?.topWall = false
    }

    private fun Context.lifecycleOwner(): LifecycleOwner? {
        var curContext = this
        var maxDepth = 20
        while (maxDepth-- > 0 && curContext !is LifecycleOwner) {
            curContext = (curContext as ContextWrapper).baseContext
        }
        return if (curContext is LifecycleOwner) {
            curContext as LifecycleOwner
        } else {
            null
        }
    }
}
