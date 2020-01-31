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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class Engine {
	public abstract String getName();
	public abstract int getEstimatedElo();

	/**
	 * Gets a future of the best move by the engine. Calls requestMove(gameContext)
	 * @param gameContext GameContext
	 * @return Move future.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public Future<Move> getBestMove(GameContext gameContext) throws ExecutionException, InterruptedException {
		Future<Move[]> movesFuture = this.requestMove(gameContext);
		Move[] moves = movesFuture.get();
		if (moves != null && moves.length > 0) {
			CompletableFuture<Move> futureMove = new CompletableFuture<Move>();
			futureMove.complete(moves[0]);
			return futureMove;
		}
		return null;
	}

	public abstract Future<Move[]> requestMove(GameContext gameContext);

	public abstract void startNewGame(GameContext gameContext);
}
