package cn.memox.ext.tip.internal

import cn.memox.ext.tip.Tip
import org.commonmark.node.Node
import org.commonmark.renderer.NodeRenderer

abstract class TipNodeRenderer : NodeRenderer {
    override fun getNodeTypes(): Set<Class<out Node>> {
        return setOf<Class<out Node>>(Tip::class.java)
    }
}