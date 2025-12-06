//package com.harutyun.xmppclient.xmpp;
//
//import com.harutyun.game.enums.CellState;
//import org.jivesoftware.smack.provider.ExtensionElementProvider;
//import org.jivesoftware.smack.packet.XmlEnvironment;
//import org.jivesoftware.smack.xml.XmlPullParser;
//import org.jivesoftware.smack.xml.XmlPullParserException;
//
//import java.io.IOException;
//
///**
// * Robust parser for <game xmlns='urn:xmpp:tic-tac-toe:0' ...> ... </game>
// */
//public class GameExtensionProvider extends ExtensionElementProvider<GameExtension> {
//
////    @Override
////    public GameExtension parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment) throws XmlPullParserException, IOException {
////
////        // parser is positioned on the START_TAG of <game>
////        String action = parser.getAttributeValue(null, "action");
////        String gameId = parser.getAttributeValue(null, "game-id");
////        String status = parser.getAttributeValue(null, "status"); // optional attribute
////
////        Integer row = null;
////        Integer col = null;
////        String symbol = null;
////
////        System.out.println(parser);
////
////        // Start reading events until we reach the matching </game> at the same depth.
////        XmlPullParser.Event event = parser.getEventType();
////        while (!(event == XmlPullParser.Event.END_ELEMENT
////                && parser.getDepth() == initialDepth
////                && GameExtension.ELEMENT.equals(parser.getName()))) {
////
////            if (event == XmlPullParser.Event.START_ELEMENT) {
////                String name = parser.getName();
////                if ("move".equals(name)) {
////                    String r = parser.getAttributeValue(null, "row");
////                    String c = parser.getAttributeValue(null, "col");
////                    String s = parser.getAttributeValue(null, "symbol"); // attribute name 'symbol'
////                    if (r != null) {
////                        row = Integer.parseInt(r);
////                    }
////                    if (c != null) {
////                        col = Integer.parseInt(c);
////                    }
////                    symbol = s;
////                } else if ("ack".equals(name) || "status".equals(name)) {
////                    // optional nested elements that may carry attributes
////                    String st = parser.getAttributeValue(null, "status");
////                    if (st != null) status = st;
////                }
////                // ignore other child elements
////            }
////
////            event = parser.next();
////        }
////
////        // Build extension; pass symbol (String) — GameExtension handles nulls.
////        return new GameExtension(action, gameId, row, col, CellState.valueOf(symbol), status);
////    }
//
//    @Override
//    public GameExtension parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment) throws XmlPullParserException, IOException {
//        String action = parser.getAttributeValue(null, "action");
//        String gameId = parser.getAttributeValue(null, "game-id");
//        String status = parser.getAttributeValue(null, "status");  // Now expect it
//        Integer row = null;
//        Integer col = null;
//        String symbol = null;
//        // ... (loop through events)
//        if (event == XmlPullParser.Event.START_ELEMENT) {
//            String name = parser.getName();
//            if ("move".equals(name)) {
//                // ...
//            } else if ("ack".equals(name) || "status".equals(name)) {
//                // ...
//            }
//        }
//        // After loop, build based on action
//        if ("move".equals(action)) {
//            if (row == null || col == null || symbol == null) {
//                throw new XmlPullParserException("Missing move data for move action");
//            }
//            return new GameExtension(action, gameId, row, col, CellState.valueOf(symbol), status);
//        } else {
//            // For "start"/"ack", use non-move ctor
//            String startSymbol = parser.getAttributeValue(null, "symbol");  // If added for start
//            if ("start".equals(action) && startSymbol != null) {
//                return new GameExtension(action, gameId, 0, 0, CellState.valueOf(startSymbol), status);  // Or custom ctor
//            }
//            return new GameExtension(action, gameId, status);  // Handles ack/start without symbol
//        }
//    }
//
//}
//
package com.harutyun.xmppclient.xmpp;

import com.harutyun.game.enums.CellState;
import com.harutyun.game.core.Move;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;

/**
 * Robust parser for <game xmlns='urn:xmpp:tic-tac-toe:0' ...> ... </game>
 */
public class GameExtensionProvider extends ExtensionElementProvider<GameExtension> {

    @Override
    public GameExtension parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment)
            throws XmlPullParserException {
        try {
            String action = parser.getAttributeValue(null, "action");
            String gameId = parser.getAttributeValue(null, "game-id");
            String status = parser.getAttributeValue(null, "status");
            String symbol = parser.getAttributeValue(null, "symbol");

            Integer row = null;
            Integer col = null;

            XmlPullParser.Event event = parser.next();

            while (!(event == XmlPullParser.Event.END_ELEMENT
                    && parser.getDepth() == initialDepth
                    && GameExtension.ELEMENT.equals(parser.getName()))) {

                if (event == XmlPullParser.Event.START_ELEMENT) {
                    String name = parser.getName();
                    if ("move".equals(name)) {
                        String r = parser.getAttributeValue(null, "row");
                        String c = parser.getAttributeValue(null, "col");
                        String s = parser.getAttributeValue(null, "symbol");
                        if (r != null) row = Integer.parseInt(r);
                        if (c != null) col = Integer.parseInt(c);
                        if (s != null) symbol = s;
                    } else if ("ack".equals(name) || "status".equals(name)) {
                        String st = parser.getAttributeValue(null, "status");
                        if (st != null) status = st;
                    }
                }

                event = parser.next();
            }

            if (row != null && col != null && symbol != null) {
                Move parsedMove;
                try {
                    parsedMove = new Move(CellState.valueOf(symbol), row, col);
                } catch (com.harutyun.game.exception.IllegalMoveException ime) {
                    throw new XmlPullParserException("Invalid move coordinates: " + ime.getMessage());
                }
                return new GameExtension(action, gameId, status, parsedMove);
            }

            if ("start".equals(action) && symbol != null) {
                return new GameExtension(action, gameId, 0, 0, CellState.valueOf(symbol), status);
            }
            return new GameExtension(action, gameId, status);

        } catch (Exception e) {
            System.err.println("[GameExtensionProvider] parse failed: " + e);
            try {
                System.err.println("[GameExtensionProvider] parser state: event=" + parser.getEventType()
                        + " name=" + parser.getName() + " depth=" + parser.getDepth());
            } catch (Exception ignored) {}
            throw e instanceof XmlPullParserException ? (XmlPullParserException) e : new XmlPullParserException(e.getMessage());
        }
    }

}