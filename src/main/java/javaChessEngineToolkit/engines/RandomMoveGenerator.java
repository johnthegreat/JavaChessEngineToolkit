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
package javaChessEngineToolkit.engines;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javaChessEngineToolkit.Engine;
import javaChessEngineToolkit.GameContext;
import javaChessEngineToolkit.Move;
import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;
import javaChessEngineToolkit.ScoringAlgorithm;

public class RandomMoveGenerator extends Engine implements ScoringAlgorithm {

	@Override
	public Future<Move[]> requestMove(final GameContext gameContext) {
		return Score(gameContext.getGame().getPosition());
	}

	@Override
	public Future<Move[]> Score(Position position) {
		short[] moves = position.getAllMoves();

		Move[] arr = new Move[moves.length];
		try {
			final Random random = new Random();
			for (int i = 0; i < moves.length; i++) {
				Position _position = new Position(position);
				_position.doMove(moves[i]);

				Move move = new Move();
				move.setShortValue(moves[i]);
				move.setNotation(_position.getLastMove().getSAN());
				// Generate a random score strength for this move
				BigDecimal bd = BigDecimal.valueOf(random.nextDouble() * 10);
				move.setScore(bd.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
				arr[i] = move;

				if (_position.canUndoMove()) {
					_position.undoMove();
				}
			}
		} catch (IllegalMoveException e) {
			e.printStackTrace(System.err);
		}
		Arrays.sort(arr);

		CompletableFuture<Move[]> completableFuture = new CompletableFuture<Move[]>();
		completableFuture.complete(arr);
		return completableFuture;
	}
	
	@Override
	public String getName() {
		return "RandomMoveGenerator";
	}

	@Override
	public int getEstimatedElo() {
		return 200;
	}

	@Override
	public void startNewGame(GameContext gameContext) {

	}
}
