package io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.basic

import io.github.lee0701.heonot.inputmethod.scripting.nodes.TreeNode

data class BasicHardKeyboardMap(val keyCode: Int, val normal: TreeNode, val shift: TreeNode) {
}