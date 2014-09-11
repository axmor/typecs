package com.axmor.eclipse.typescript.editor.parser;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;

/**
 * A rule for detecting patterns corresponding to comments
 * 
 * @author Asya Vorobyova
 * 
 */
public class JavaDocCommentRuler extends MultiLineRule {
    /**
     * A constructor
     * 
     * @param token the token to be returned on success
     */
    public JavaDocCommentRuler(IToken token) {
        super("/**", "*/", token);
    }

    @Override
    protected boolean sequenceDetected(ICharacterScanner scanner, char[] sequence, boolean eofAllowed) {
        int c = scanner.read();
        System.out.println("--------------");
        System.out.println(sequence);
        System.out.println((char) c);

        if (sequence[0] == '/') {
            return true;
        } else if (sequence[0] == '/') {
            scanner.unread();
        }
        return super.sequenceDetected(scanner, sequence, eofAllowed);
    }

}
