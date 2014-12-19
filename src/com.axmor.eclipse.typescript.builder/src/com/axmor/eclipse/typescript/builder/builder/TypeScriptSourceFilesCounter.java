package com.axmor.eclipse.typescript.builder.builder;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * {@link IResourceVisitor} that counts TypeScript source files
 * 
 * @author Ats Uiboupin
 */
class TypeScriptSourceFilesCounter implements IResourceVisitor {
    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public boolean visit(IResource resource) throws CoreException {
        if (TypescriptBuilder.isTypeScriptFileToCompile(resource)) {
            counter.incrementAndGet();
        }
        return true;
    }

    int count(IResource res) {
        new IResourceVisitorHelper(this).accept(res);
        return counter.get();
    }

    int count(IResourceDelta delta) throws CoreException {
        new IResourceVisitorHelper(this).accept(delta);
        return counter.get();
    }
}