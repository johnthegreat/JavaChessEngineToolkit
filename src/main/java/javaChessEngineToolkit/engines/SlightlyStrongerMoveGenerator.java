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

import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;
import javaChessEngineToolkit.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class SlightlyStrongerMoveGenerator extends Engine implements ScoringAlgorithm {
	@Override
	public String getName() {
		return "SlightlyStrongerMoveGenerator";
	}

	@Override
	public int getEstimatedElo() {
		return 300;
	}

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
				if (_position.isMate()) {
					// This move scores highly, so the engine should make this move.
					move.setScore(99.99);
				} else if (_position.isStaleMate()) {
					// This move makes it stalemate, avoid this if we can.
					move.setScore(0.01);
				} else if (ChesspressoUtils.hasInsufficientMatingMaterial(position)) {
					// This move results in insufficient mating material, avoid this if we can.
					move.setScore(0.01);
				} else {
					// Set the score here.
					BigDecimal bd = BigDecimal.valueOf(random.nextDouble() * 10);
					move.setScore(bd.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
				}

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
	public void startNewGame(GameContext gameContext) {

	}
}
