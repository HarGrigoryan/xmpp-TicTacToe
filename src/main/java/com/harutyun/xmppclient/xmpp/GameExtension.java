package com.harutyun.xmppclient.xmpp;

import com.harutyun.game.core.Move;
import com.harutyun.game.enums.CellState;
import com.harutyun.game.exception.IllegalMoveException;
import lombok.Getter;
import lombok.Setter;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.util.XmlStringBuilder;

import javax.xml.namespace.QName;

@Getter
public class GameExtension implements ExtensionElement {

    public static final String ELEMENT = "game";
    public static final String NAMESPACE = "urn:xmpp:tic-tac-toe:0";

    @Setter
    private String action;
    private final String gameId;
    @Setter
    private String status;

    private final Move move;

//    public GameExtension(String action, String gameId, int row, int col, CellState cellState) throws IllegalMoveException {
//        this.action = action;
//        this.gameId = gameId;
//        this.move = new Move(cellState, row, col);
//        this.status = "ok";
//    }

    public GameExtension(String action, String gameId, int row, int col, CellState cellState, String status) throws IllegalMoveException {
        this.action = action;
        this.gameId = gameId;
        this.move = new Move(cellState, row, col);
        this.status = status;
    }

    public GameExtension(String action, String gameId, String status) {
        this.action = action;
        this.gameId = gameId;
        this.status = status;
        this.move = null;
    }

    public GameExtension(String action, String gameId, String status, Move move) {
        this.action = action;
        this.gameId = gameId;
        this.move = move;
        this.status = status;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public QName getQName() {
        return new QName(NAMESPACE, ELEMENT);
    }

    @Override
    public String getLanguage() {
        return null;
    }

    /**
     * Smack will call this variant when marshalling with an XmlEnvironment.
     * Delegate to the older toXML(String) implementation for simplicity.
     */
    @Override
    public CharSequence toXML(XmlEnvironment xmlEnvironment) {
        return toXML((String) null);
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        XmlStringBuilder xml = new XmlStringBuilder(this);

        // attributes must be added BEFORE rightAngleBracket()
        if (action != null) {
            xml.attribute("action", action);
        }
        if (gameId != null) {
            xml.attribute("game-id", gameId);
        }
        if (status != null) {
            xml.attribute("status", status);
        }
        if ("start".equals(action) && move != null) {
            xml.attribute("symbol", move.getCellState().toString());
        }

        xml.rightAngleBracket();

        if (move != null) {
            xml.halfOpenElement("move");
            xml.attribute("row", Integer.toString(move.getRow()));
            xml.attribute("col", Integer.toString(move.getCol()));
            xml.attribute("symbol", move.getCellState().toString());
            xml.closeEmptyElement();
        }

        xml.closeElement(ELEMENT);
        return xml;
    }


    /**
     * Default no-arg toXML() should delegate to the XmlEnvironment-aware one.
     */
    @Override
    public CharSequence toXML() {
        return toXML((XmlEnvironment) null);
    }

    @Override
    public String toString() {
        return "GameExtension[" +
                "\n action=" + action +
                "\n gameId=" + gameId +
                "\n status=" + status +
                "\n move=" + move +
                "\n]";

    }
}
