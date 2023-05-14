package cn.memox.utils

/**
 * 从正文中提取图片
 * @return List<Pair<图片名称,图片链接>>
 */
val String.pickImages
    get() = Regex("!\\[([\\W\\w]*?)]\\(([\\S]+?)\\)")
        .findAll(this).map {
            Pair(it.groupValues[1], it.groupValues[2])
        }.take(9).toSet()

val String.removeImages
    get() =
        replace(Regex("!\\[]\\((\\S+?)\\)"), "[图片]")
            .replace(Regex("!\\[([\\W\\w]*?)]\\((\\S+?)\\)"), "[$1]")
            .trim()