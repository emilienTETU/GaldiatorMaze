package com.example.gladiateur

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment

class ArcadeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mode.mode = GameMode.ARCADE
        val root = inflater.inflate(R.layout.fragment_arcade, container, false)
        setHasOptionsMenu(true);
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_arcade_maze, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.maze_1 -> Mode.mazeSelected.postValue(1)
            R.id.maze_2 -> Mode.mazeSelected.postValue(2)
            R.id.maze_3 -> Mode.mazeSelected.postValue(3)
            R.id.maze_4 -> Mode.mazeSelected.postValue(4)
            R.id.maze_5 -> Mode.mazeSelected.postValue(5)
            R.id.maze_6 -> Mode.mazeSelected.postValue(6)
            R.id.maze_7 -> Mode.mazeSelected.postValue(7)
            R.id.maze_8 -> Mode.mazeSelected.postValue(8)
            R.id.maze_9 -> Mode.mazeSelected.postValue(9)
            R.id.maze_10 -> Mode.mazeSelected.postValue(10)
            R.id.maze_11 -> Mode.mazeSelected.postValue(11)
            R.id.maze_12 -> Mode.mazeSelected.postValue(12)
            R.id.maze_Bonus -> Mode.mazeSelected.postValue(13)
            else -> Mode.mazeSelected.postValue(1)
        }
        return super.onOptionsItemSelected(item)
    }
}
