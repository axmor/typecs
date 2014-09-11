/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.sourcemap;

import java.io.File;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.debug.Activator;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * SourceMap format parser based on <a href=
 * 'https://github.com/google/closure-compiler/tree/master/src/com/google/debugging/sourcemap'>Googl
 * e Closure Compiler SourceMap parser</a>
 * 
 * @author Konstantin Zaitcev
 */
public class SourceMapParser {
    /** A Base64 VLQ digit can represent 5 bits, so it is base-32. */
    private static final int VLQ_BASE_SHIFT = 5;
    /** A Base64 VLQ digit can represent 5 bits, so it is base-32. */
    private static final int VLQ_BASE = 1 << VLQ_BASE_SHIFT;
    /** A mask of bits for a VLQ digit (11111), 31 decimal. */
    private static final int VLQ_BASE_MASK = VLQ_BASE - 1;
    /** The continuation bit is the 6th bit. */
    private static final int VLQ_CONTINUATION_BIT = VLQ_BASE;

    /** Base64 alphabet. */
    private static final String BASE64_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    /** Base64 alphabet mapping. */
    private static final int[] BASE64_DECODE_SYMBOLS = new int[256];

    static {
        for (int i = 0; i < BASE64_SYMBOLS.length(); i++) {
            BASE64_DECODE_SYMBOLS[(int) BASE64_SYMBOLS.charAt(i)] = i;
        }
    }

    /**
     * @param file
     *            source map file
     * @return parsed source map
     */
    public SourceMap parse(File file) {
        try {
            JSONObject json = new JSONObject(Files.toString(file, Charsets.UTF_8));
            SourceMap smap = new SourceMap();
            smap.setVersion(json.getInt("version"));
            smap.setFile(new File(file.getParentFile(), json.getString("file")).getCanonicalPath());
            JSONArray names = json.getJSONArray("names");
            for (int i = 0; i < names.length(); i++) {
                smap.getNames().add(names.getString(i));
            }

            String mappings = json.getString("mappings");
            String[] lines = mappings.split(";");
            int tsLinePrev = 0;
            int tsColumnPrev = 0;
            for (int i = 0; i < lines.length; i++) {
                int jsColumnPrev = 0;
                int tsFileIdxPrev = 0;
                String line = lines[i];
                String[] segments = line.split(",");
                for (String segment : segments) {
                    StringCharacterIterator str = new StringCharacterIterator(segment);
                    ArrayList<Integer> seg = new ArrayList<>();
                    while (str.current() != StringCharacterIterator.DONE) {
                        seg.add(decode(str));
                    }
                    if (seg.size() >= 3) {
                        SourceMapItem item = new SourceMapItem();
                        item.setJsLine(i);
                        item.setJsFile(json.getString("file"));
                        jsColumnPrev += seg.get(0);
                        tsFileIdxPrev += seg.get(1);
                        tsLinePrev += seg.get(2);
                        tsColumnPrev += seg.get(3);
                        item.setJsColumn(jsColumnPrev);
                        item.setTsLine(tsLinePrev + 1);
                        item.setTsColumn(tsColumnPrev);
                        item.setTsFile(json.getJSONArray("sources").getString(tsFileIdxPrev));
                        smap.getMaps().add(item);
                    }
                }
            }
            return smap;
        } catch (Exception e) {
            Activator.error(e);
        }
        return null;
    }

    /**
     * Decodes string iterator into int value used VLQ format.
     * 
     * @param str
     *            string iterator
     * @return int value
     */
    private int decode(CharacterIterator str) {
        int result = 0;
        boolean continuation = true;
        int shift = 0;
        char c = str.current();
        do {
            if (c == CharacterIterator.DONE) {
                return 0;
            }
            int digit = BASE64_DECODE_SYMBOLS[c];
            continuation = (digit & VLQ_CONTINUATION_BIT) != 0;
            digit &= VLQ_BASE_MASK;
            result = result + (digit << shift);
            shift = shift + VLQ_BASE_SHIFT;
            if (continuation) {
                c = str.next();
            }
        } while (continuation);
        str.next();
        return ((result & 1) == 1) ? -(result >> 1) : (result >> 1);
    }
}
