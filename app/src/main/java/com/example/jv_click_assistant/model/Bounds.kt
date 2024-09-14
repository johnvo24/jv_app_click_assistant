package com.example.jv_click_assistant.model

class Bounds(var left: Int, var top: Int, var width: Int, var height: Int) {
    fun getStart(): Point {
        return Point(top, left);
    }
    fun print(): String {
        return "Bounds(top: $top, left: $left, width: $width, height: $height)";
    }
}