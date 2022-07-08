package org.utbot.intellij.plugin.ui

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ExternalLibraryDescriptor
import com.intellij.openapi.roots.JavaProjectModelModificationService
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ModuleSourceOrderEntry
import com.intellij.openapi.ui.Messages
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.thenRun
import org.utbot.framework.plugin.api.MockFramework
import org.utbot.intellij.plugin.ui.utils.LibrarySearchScope
import org.utbot.intellij.plugin.ui.utils.allLibraries
import org.utbot.intellij.plugin.ui.utils.findFrameworkLibrary
import org.utbot.intellij.plugin.ui.utils.parseVersion

fun createMockFrameworkNotificationDialog(title: String) = Messages.showYesNoDialog(
    """Mock framework ${MockFramework.MOCKITO.displayName} is not installed into current module. 
            |Would you like to install it now?""".trimMargin(),
    title,
    "Yes",
    "No",
    Messages.getQuestionIcon(),
)

fun configureMockFramework(project: Project, module: Module) {
    val selectedMockFramework = MockFramework.MOCKITO

    val libraryInProject =
        findFrameworkLibrary(project, module, selectedMockFramework, LibrarySearchScope.Project)
    val versionInProject = libraryInProject?.libraryName?.parseVersion()

    selectedMockFramework.isInstalled = true
    addDependency(project, module, mockitoCoreLibraryDescriptor(versionInProject))
        .onError { selectedMockFramework.isInstalled = false }
}

/**
 * Adds the dependency for selected framework via [JavaProjectModelModificationService].
 *
 * Note that version restrictions will be applied only if they are present on target machine
 * Otherwise latest release version will be installed.
 */
fun addDependency(project: Project, module: Module, libraryDescriptor: ExternalLibraryDescriptor): Promise<Void> {
    val promise = JavaProjectModelModificationService
        .getInstance(project)
        //this method returns JetBrains internal Promise that is difficult to deal with, but it is our way
        .addDependency(module, libraryDescriptor, DependencyScope.TEST)
    promise.thenRun {
        module.allLibraries()
            .firstOrNull { library -> library.libraryName == libraryDescriptor.presentableName }?.let {
                ModuleRootModificationUtil.updateModel(module) { model -> placeEntryToCorrectPlace(model, it) }
            }
    }
    return promise
}

fun placeEntryToCorrectPlace(model: ModifiableRootModel, addedEntry: LibraryOrderEntry) {
    val order = model.orderEntries
    val lastEntry = order.last()
    if (lastEntry is LibraryOrderEntry && lastEntry.library == addedEntry.library) {
        val insertionPoint = order.indexOfFirst { it is ModuleSourceOrderEntry } + 1
        if (insertionPoint > 0) {
            System.arraycopy(order, insertionPoint, order, insertionPoint + 1, order.size - 1 - insertionPoint)
            order[insertionPoint] = lastEntry
            model.rearrangeOrderEntries(order)
        }
    }
}
