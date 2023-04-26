import com.intellij.codeInsight.folding.CodeFoldingManager
import com.intellij.codeInsight.folding.impl.EditorFoldingInfo
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.psi.PsiDocCommentBase
import com.intellij.psi.PsiElement
import liveplugin.AnAction
import liveplugin.PluginUtil.registerAction
import liveplugin.PluginUtil.show
import liveplugin.editor

registerAction("FindAllJavaClassDependencies", "", "FoldingGroup", "Collapse To Definitions", AnAction {
    val editor = it.editor
        ?: return@AnAction

    ActionManager.getInstance().getAction("CollapseAllRegions").actionPerformed(it)

    val foldingManager = CodeFoldingManager.getInstance(project)
    for (region in editor.foldingModel.allFoldRegions) {
        val element = EditorFoldingInfo.get(editor).getPsiElement(region)?.toMyElement()
            ?: continue
        region.isExpanded = with (element) { !isFunctionOrMethod() && !isImportList() }
/*
        if (element is PsiDocCommentBase) {
            region.isExpanded = true
        }
        if ("${element?.javaClass}".endsWith(".KtClassBody")) {
            region.isExpanded = true
        }
*/
        //show("${element.className}, parent: ${element.parent?.className}")
    }
    foldingManager.updateFoldRegions(editor)
})

class MyElement(val element: PsiElement) {
    val className: String = element.javaClass.simpleName
}

fun PsiElement.toMyElement() =
    MyElement(this)

val MyElement?.parent: MyElement?
    get() = this?.element?.parent?.toMyElement()

fun MyElement?.isNamedFunction() =
    this?.className == "KtNamedFunction"

fun MyElement?.isBlockExpression() =
    this?.className == "KtBlockExpression"

fun MyElement?.isClassInitializer() =
    this?.className == "KtClassInitializer"

fun MyElement?.isClass() =
    this?.className == "KtClass"

fun MyElement?.isBody() =
    this?.className == "KtClassBody"

fun MyElement?.isPropertyAccessor() =
    this?.className == "KtPropertyAccessor"

fun MyElement?.isFunctionOrMethod() =
    isNamedFunction() ||
    (isBlockExpression() && with(parent) { isNamedFunction() || isPropertyAccessor() || isClassInitializer() })

fun MyElement?.isImportList() =
    this?.className == "KtImportList"

//fun PsiElement.isFunctionOrMethod() =
//    this is org.jetbrains.kotlin.psi.KtNamedFunction

//= with ("${this?.javaClass}") {
//    endsWith(".KtNamedFunction") /*|| (endsWith(".KtBlockExpression") && this@isFunctionOrMethod?.parent.isClass() )*/
//}

//fun PsiElement.isClass() =
//    this is org.jetbrains.kotlin.psi.KtClassBody

//fun PsiElement.isImportList() =
//    this is org.jetbrains.kotlin.psi.KtImportList

//fun PsiElement?.isImportList() = with ("${this?.javaClass}") {
//    endsWith(".KtImportList")
//}
