package com.axmor.eclipse.typescript.builder.builder;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Helper class that enables to use {@link IResourceVisitor} the same way with both
 * {@link IResource} and {@link IResourceDelta}
 * 
 * @author Ats Uiboupin
 */
class IResourceVisitorHelper {
    private final IResourceVisitor visitor;

    IResourceVisitorHelper(IResourceVisitor visitor) {
        this.visitor = visitor;
    }

    void accept(IResource res) {
        try {
            res.accept(visitor);
        } catch (CoreException e) {
            throw new RuntimeException("Failed to visit resource " + res + " using visitor " + visitor, e);
        }
    }

    void accept(IResourceDelta delta) throws CoreException {
        delta.accept(new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta delta) throws CoreException {
                return visitor.visit(delta.getResource());
            }
        });
    }

}