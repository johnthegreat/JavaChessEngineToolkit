/*
 * MIT License
 *
 * Copyright (c) 2020 John Nahlen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package javaChessEngineToolkit;

import chesspresso.Chess;
import chesspresso.position.Position;

public class BoardRenderUtils {
    // Renders a basic graphical display of the board (an 8x8 grid with the pieces, separated by spaces)
    public static String draw(final Position position) {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=Chess.NUM_OF_ROWS-1;i>=0;i--) {
            for(int j=0;j<Chess.NUM_OF_COLS;j++) {
                int sqi = Chess.coorToSqi(j,i);
                int stone = position.getStone(sqi);
                String piece = Character.toString(Chess.stoneToChar(stone));
                stringBuilder.append(Chess.stoneHasColor(stone,Chess.WHITE) ? piece.toUpperCase() : piece.toLowerCase()).append(" ");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
