package cn.memox.ext.fold

import cn.memox.ext.fold.internal.TipBlockProcessor
import cn.memox.ext.fold.internal.TipHtmlNodeRenderer
import cn.memox.ext.fold.internal.TipTextContentNodeRenderer
import org.commonmark.Extension
import org.commonmark.parser.Parser
import org.commonmark.parser.Parser.ParserExtension
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.html.HtmlRenderer.HtmlRendererExtension
import org.commonmark.renderer.text.TextContentRenderer
import org.commonmark.renderer.text.TextContentRenderer.TextContentRendererExtension

/**
 * Extension for ins using :::
 *
 *
 * Create it with [.create] and then configure it on the builders
 * ([org.commonmark.parser.Parser.Builder.extensions],
 * [HtmlRenderer.Builder.extensions]).
 *
 *
 *
 * The parsed ins text regions are turned into [Tip] nodes.
 *
 */
class TipExtension private constructor() : ParserExtension, HtmlRendererExtension,
    TextContentRendererExtension {
    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customBlockParserFactory(TipBlockProcessor.Factory())
    }

    override fun extend(rendererBuilder: HtmlRenderer.Builder) {
        rendererBuilder.nodeRendererFactory { context -> TipHtmlNodeRenderer(context) }
    }

    override fun extend(rendererBuilder: TextContentRenderer.Builder) {
        rendererBuilder.nodeRendererFactory { context -> TipTextContentNodeRenderer(context) }
    }

    companion object {
        fun create(): Extension {
            return TipExtension()
        }
    }
}