package com.axmor.eclipse.typescript.editor.hierarchy;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.axmor.eclipse.typescript.editor.Activator;
import com.google.common.base.Throwables;

public class CallHierarchyViewPart extends ViewPart implements IDoubleClickListener {

    public static final String ID_CALL_HIERARCHY = "com.axmor.eclipse.typescript.editor.hierarchy.view";
    
    private TypeScriptHierarchyViewer fCallHierarchyViewer;
    private IPartListener2 fPartListener;
    
    @Override
    public void doubleClick(DoubleClickEvent event) {
        if (event.getViewer() == fCallHierarchyViewer) {
            methodSelectionChanged(event.getSelection());
        }
    }
    
    private void methodSelectionChanged(ISelection selection) {
        if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() == 1) {
            Object selectedElement = ((IStructuredSelection) selection).getFirstElement();
            if (selectedElement == null) {
                return;
            }
            TreeRoot elem = (TreeRoot) selectedElement;
            IFile file = elem.getFile();
            int offset = elem.getCallOffset();
            int length = elem.getCallLength();
            IEditorPart newEditor;
            try {
                newEditor = IDE.openEditor(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage(), file, true);
                if (newEditor != null && newEditor instanceof AbstractTextEditor) {
                    ((AbstractTextEditor) newEditor).selectAndReveal(offset, length);
                }
            } catch (PartInitException e) {
                throw Throwables.propagate(e);
            }
        }
    }    
    

    @Override
    public void createPartControl(Composite parent) {
        fCallHierarchyViewer = new TypeScriptHierarchyViewer(parent, this);
        fCallHierarchyViewer.addDoubleClickListener(this);
        addPartListener();
    }
    
    private boolean isThisView(IWorkbenchPartReference partRef) {
        if (!ID_CALL_HIERARCHY.equals(partRef.getId())) {
            return false;
        }            
        String partRefSecondaryId= ((IViewReference)partRef).getSecondaryId();
        String thisSecondaryId= getViewSite().getSecondaryId();
        return thisSecondaryId == null && partRefSecondaryId == null || thisSecondaryId != null && 
                thisSecondaryId.equals(partRefSecondaryId);
    }
    
    private void addPartListener() {
        fPartListener= new IPartListener2() {
            /* (non-Javadoc)
             * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
             */
            public void partActivated(IWorkbenchPartReference partRef) {
                if (isThisView(partRef)) {
                    TypeScriptHierarchyUI.getDefault().callHierarchyViewActivated(CallHierarchyViewPart.this);
                }
            }

            public void partBroughtToTop(IWorkbenchPartReference partRef) { }

            /* (non-Javadoc)
             * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
             */
            public void partClosed(IWorkbenchPartReference partRef) {
                if (isThisView(partRef)) {
                    TypeScriptHierarchyUI.getDefault().callHierarchyViewClosed(CallHierarchyViewPart.this);
                }
            }
            
            public void partDeactivated(IWorkbenchPartReference partRef) {}
            public void partOpened(IWorkbenchPartReference partRef) { }
            public void partHidden(IWorkbenchPartReference partRef) { }
            public void partVisible(IWorkbenchPartReference partRef) { }
            public void partInputChanged(IWorkbenchPartReference partRef) { }
        };
        getViewSite().getPage().addPartListener(fPartListener);
    }

    @Override
    public void setFocus() {
        fCallHierarchyViewer.getControl().setFocus();
    }    

    /**
     * @param result
     */
    public void setInputElements(TreeRoot[] treeRoots) {        
        fCallHierarchyViewer.setInput(treeRoots);
    }
    
    public TypeScriptHierarchyViewer getViewer() {
        return fCallHierarchyViewer;
    }

}
