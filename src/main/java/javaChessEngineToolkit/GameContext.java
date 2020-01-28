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

import chesspresso.game.Game;

public class GameContext {
    protected Game game;
    protected ChessClock chessClock;
    protected Engine whiteEngine;
    protected Engine blackEngine;
    protected boolean isTimed = false;

    public GameContext(Game game, ChessClock chessClock, Engine whiteEngine, Engine blackEngine) {
        this.game = game;
        this.chessClock = chessClock;
        this.whiteEngine = whiteEngine;
        this.blackEngine = blackEngine;
        if (this.chessClock != null) {
            this.isTimed = true;
        }
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public ChessClock getChessClock() {
        return chessClock;
    }

    public void setChessClock(ChessClock chessClock) {
        this.chessClock = chessClock;
    }

    public Engine getWhiteEngine() {
        return whiteEngine;
    }

    public void setWhiteEngine(Engine whiteEngine) {
        this.whiteEngine = whiteEngine;
    }

    public Engine getBlackEngine() {
        return blackEngine;
    }

    public void setBlackEngine(Engine blackEngine) {
        this.blackEngine = blackEngine;
    }

    public boolean isTimed() {
        return isTimed;
    }
}
