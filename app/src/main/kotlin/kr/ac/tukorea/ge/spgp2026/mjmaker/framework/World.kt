package kr.ac.tukorea.ge.spgp2026.mjmaker.framework

import android.graphics.Canvas

class World<TLayer>(orderedLayers: Array<TLayer>) {
    private val layers = orderedLayers.associateWith { mutableListOf<GameObject>() }

    fun add(layer: TLayer, obj: GameObject) {
        layers.getValue(layer).add(obj)
    }

    fun remove(layer: TLayer, obj: GameObject): Boolean {
        return layers.getValue(layer).remove(obj)
    }

    fun update(frameTime: Float) {
        for (layer in layers.values) {
            // 역순 순회를 통해 업데이트 중 삭제(remove) 시 인덱스 꼬임 방지
            for (i in layer.lastIndex downTo 0) {
                layer[i].update(frameTime)
            }
        }
    }

    fun draw(canvas: Canvas) {
        for (layer in layers.values) {
            for (obj in layer) {
                obj.draw(canvas)
            }
        }
    }

    fun objectsAt(layer: TLayer): List<GameObject> = layers.getValue(layer)
}
