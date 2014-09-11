/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.internal.ui.text.FileLabelProvider;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.internal.ui.text.LineElement;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Page instance to display search results
 * 
 * @author Asya Vorobyova
 * 
 */
@SuppressWarnings("restriction")
public class TypeScriptSearchResultPage extends AbstractTextSearchViewPage {

    /** An empty array **/
    private static final Object[] EMPTY_ARR = new Object[0];

    /**
     *  The maximal number of top level elements to be shown in a viewer
     */
    private static final int DEFAULT_ELEMENT_LIMIT = 1000;

    /**
     * Provider to organize page content
     */
    private TypeScriptSearchContentProvider contentProvider;

    /**
     * Constructor
     */
    public TypeScriptSearchResultPage() {
        super();
        setElementLimit(new Integer(DEFAULT_ELEMENT_LIMIT));
    }

    @Override
    protected void showMatch(Match match, int currentOffset, int currentLength, boolean activate)
            throws PartInitException {
        IFile file = (IFile) match.getElement();
        IWorkbenchPage page = getSite().getPage();
        if (currentOffset >= 0 && currentLength != 0) {
            openAndSelect(page, file, currentOffset, currentLength, activate);
        } else {
            open(page, file, activate);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void evaluateChangedElements(Match[] matches, @SuppressWarnings("rawtypes") Set changedElements) {
        for (int i = 0; i < matches.length; i++) {
            changedElements.add(((FileMatch) matches[i]).getLineElement());
        }
    }

    @Override
    protected void elementsChanged(Object[] objects) {
        if (contentProvider != null) {
            contentProvider.elementsChanged(objects);
        }    
    }

    @Override
    protected void clear() {
        if (contentProvider != null) {
            contentProvider.clear();
        }
    }

    @Override
    public int getDisplayedMatchCount(Object element) {
        if (element instanceof LineElement) {
            LineElement lineEntry = (LineElement) element;
            return lineEntry.getNumberOfMatches(getInput());
        }
        return 0;
    }

    @Override
    public Match[] getDisplayedMatches(Object element) {
        if (element instanceof LineElement) {
            LineElement lineEntry = (LineElement) element;
            return lineEntry.getMatches(getInput());
        }
        return new Match[0];
    }

    @Override
    protected void configureTreeViewer(TreeViewer viewer) {
        viewer.setUseHashlookup(true);
        FileLabelProvider innerLabelProvider = new FileLabelProvider(this, FileLabelProvider.SHOW_LABEL);
        viewer.setLabelProvider(innerLabelProvider);
        contentProvider = new TypeScriptSearchContentProvider(viewer);
        viewer.setContentProvider(contentProvider);
    }

    @Override
    protected void configureTableViewer(TableViewer viewer) {
        throw new IllegalStateException("Doesn't support table mode.");
    }

    /**
     * Class that organizes content view for a tree viewer
     */
    private class TypeScriptSearchContentProvider implements ITreeContentProvider {
        /** Search result **/
        private AbstractTextSearchResult searchResult;

        /** Map stored tree structure **/
        private Map<Object, Set<Object>> childrenMap;

        /** Current tree viewer **/
        private AbstractTreeViewer treeViewer;

        /**
         * Constructor
         * 
         * @param treeViewer current viewer
         */
        public TypeScriptSearchContentProvider(AbstractTreeViewer treeViewer) {
            super();
            this.treeViewer = treeViewer;
        }

        @Override
        public void dispose() {
            // nothing to do
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            initialize((AbstractTextSearchResult) newInput);
        }

        /**
         * Initializes tree structure for a given result
         * 
         * @param result search result
         */
        private synchronized void initialize(AbstractTextSearchResult result) {
            searchResult = result;
            childrenMap = new HashMap<Object, Set<Object>>();
            if (result != null) {
                Object[] elements = result.getElements();
                for (int i = 0; i < elements.length; i++) {
                    Match[] matches = result.getMatches(elements[i]);
                    for (int j = 0; j < matches.length; j++) {
                        insert(((FileMatch) matches[j]).getLineElement(), false);
                    }
                }
            }
        }

        /**
         * Makes inserting and viewer redrawing
         * 
         * @param child child to insert into tree
         * @param refreshViewer flag to determine if refreshing need
         */
        private void insert(Object child, boolean refreshViewer) {
            Object parent = getParent(child);
            while (parent != null) {
                if (insertChild(parent, child)) {
                    if (refreshViewer) {
                        treeViewer.add(parent, child);
                    }    
                } else {
                    if (refreshViewer) {
                        treeViewer.refresh(parent);
                    }    
                    return;
                }
                child = parent;
                parent = getParent(child);
            }
            if (insertChild(searchResult, child)) {
                if (refreshViewer) {
                    treeViewer.add(searchResult, child);
                }    
            }
        }

        /**
         * Adds the child to the parent.
         * 
         * @param parent
         *            the parent
         * @param child
         *            the child
         * @return <code>true</code> if this set did not already contain the specified element
         */
        private boolean insertChild(Object parent, Object child) {
            Set<Object> children = (Set<Object>) childrenMap.get(parent);
            if (children == null) {
                children = new HashSet<Object>();
                childrenMap.put(parent, children);
            }
            return children.add(child);
        }

        /**
         * Checks if a given parent has a given child
         * 
         * @param parent 
         * @param child 
         * @return true if a given parent has a given child
         */
        private boolean hasChild(Object parent, Object child) {
            Set<Object> children = (Set<Object>) childrenMap.get(parent);
            return children != null && children.contains(child);
        }

        /**
         * Tries to make removing and viewer redrawing
         * 
         * @param element object to insert into tree
         * @param refreshViewer flag to determine if refreshing need
         */        
        private void remove(Object element, boolean refreshViewer) {
            // precondition here: searchResult.getMatchCount(child) <= 0

            if (hasChildren(element)) {
                if (refreshViewer) {
                    treeViewer.refresh(element);
                }    
            } else {
                if (!hasMatches(element)) {
                    childrenMap.remove(element);
                    Object parent = getParent(element);
                    if (parent != null) {
                        removeFromSiblings(element, parent);
                        remove(parent, refreshViewer);
                    } else {
                        removeFromSiblings(element, searchResult);
                        if (refreshViewer) {
                            treeViewer.refresh();
                        }    
                    }
                } else {
                    if (refreshViewer) {
                        treeViewer.refresh(element);
                    }
                }
            }
        }

        /**
         * Checks if a given element has matches in a search result
         * 
         * @param element current 
         * @return true if there are matches
         */
        private boolean hasMatches(Object element) {
            if (element instanceof LineElement) {
                LineElement lineElement = (LineElement) element;
                return lineElement.getNumberOfMatches(searchResult) > 0;
            }
            return searchResult.getMatchCount(element) > 0;
        }

        /**
         * Tries to make removing of a given element from parent's children
         * 
         * @param element candidate to a child
         * @param parent parent element
         */
        private void removeFromSiblings(Object element, Object parent) {
            Set<Object> siblings = (Set<Object>) childrenMap.get(parent);
            if (siblings != null) {
                siblings.remove(element);
            }
        }

        /**
         * Tries to update tree structure and viewer
         * 
         * @param updatedElements elements to update
         */
        public void elementsChanged(Object[] updatedElements) {
            if (searchResult == null) {
                return;
            }
            for (int i = 0; i < updatedElements.length; i++) {
                // change events to line elements are reported in text search
                LineElement lineElement = (LineElement) updatedElements[i];
                int nMatches = lineElement.getNumberOfMatches(searchResult);
                if (nMatches > 0) {
                    if (hasChild(lineElement.getParent(), lineElement)) {
                        treeViewer.update(new Object[] { lineElement, lineElement.getParent() }, null);
                    } else {
                        insert(lineElement, true);
                    }
                } else {
                    remove(lineElement, true);
                }

            }
        }

        /**
         * Refreshes provider
         */
        public void clear() {
            initialize(searchResult);
            treeViewer.refresh();
        }

        @Override
        public Object[] getElements(Object inputElement) {
            Object[] children = getChildren(inputElement);
            int elementLimit = getElementLimit();
            if (elementLimit != -1 && elementLimit < children.length) {
                Object[] limitedChildren = new Object[elementLimit];
                System.arraycopy(children, 0, limitedChildren, 0, elementLimit);
                return limitedChildren;
            }
            return children;
        }

        /**
         * Gets limit number of tree elements
         * 
         * @return limit number
         */
        private int getElementLimit() {
            return TypeScriptSearchResultPage.this.getElementLimit().intValue();
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            Set<Object> children = (Set<Object>) childrenMap.get(parentElement);
            if (children == null) {
                return EMPTY_ARR;
            }    
            return children.toArray();
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof IResource) {
                return null;
            }
            if (element instanceof LineElement) {
                return ((LineElement) element).getParent();
            }
            if (element instanceof FileMatch) {
                FileMatch match = (FileMatch) element;
                return match.getLineElement();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

    }

}
