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
import chesspresso.game.Game;
import chesspresso.move.IllegalMoveException;
import chesspresso.pgn.PGN;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class EngineMatch {
	private Engine whiteEngine;
	private Engine blackEngine;
	private EngineMatchOptions engineMatchOptions;
	
	public EngineMatch(Engine whiteEngine,Engine blackEngine,EngineMatchOptions engineMatchOptions) {
		this.whiteEngine = whiteEngine;
		this.blackEngine = blackEngine;
		this.engineMatchOptions = engineMatchOptions;
	}
	
	public EngineMatchResult play() throws IllegalMoveException, ExecutionException, InterruptedException {
		ChessClock chessClock = null;
		if (engineMatchOptions.isTimed()) {
			chessClock = new ChessClock(engineMatchOptions.getTime()*60*1000,engineMatchOptions.getTime()*60*1000);
		}

		String gameReason = null;
		GameResult gameResult = null;
		EngineMatchResult engineMatchResult;
		
		final Game game = new Game();
		game.setTag(PGN.TAG_WHITE,whiteEngine.getName());
		game.setTag(PGN.TAG_WHITE_ELO,String.valueOf(whiteEngine.getEstimatedElo()));
		game.setTag(PGN.TAG_BLACK,blackEngine.getName());
		game.setTag(PGN.TAG_BLACK_ELO,String.valueOf(blackEngine.getEstimatedElo()));

		GameContext gameContext = new GameContext(game,chessClock,whiteEngine,blackEngine);
		whiteEngine.startNewGame(gameContext);
		blackEngine.startNewGame(gameContext);

//		final ChessClock finalChessClock = chessClock;

		List<Runnable> onEveryMoveHalfMoveRunnables = new ArrayList<Runnable>();
		onEveryMoveHalfMoveRunnables.add(new Runnable() {
			@Override
			public void run() {
				System.out.println(game.getPosition().getFEN());

//				if (finalChessClock != null) {
//					System.out.println(String.format("(%s) (%s)",ChessClockUtils.formatTime(finalChessClock.getClockForColor(Chess.WHITE)),ChessClockUtils.formatTime(finalChessClock.getClockForColor(Chess.BLACK))));
//				}
			}
		});
		
		while(game.getPosition().canMove()) {
			boolean shouldAddInc = game.getCurrentMoveNumber() > 0;

			if (chessClock != null) {
				if (chessClock.getClockForColor(Chess.WHITE) <= 0) {
					gameResult = GameResult.BLACK_WIN;
					gameReason = "White forfeits on time.";
					break;
				}
				if (chessClock.getClockForColor(Chess.BLACK) <= 0) {
					gameResult = GameResult.WHITE_WIN;
					gameReason = "Black forfeits on time.";
					break;
				}
			}

			if (ChesspressoUtils.isDraw(game.getPosition())) {
				gameResult = GameResult.DRAW;
				gameReason = "Game drawn.";
				break;
			}

			if (chessClock != null) {
				chessClock.startClock(Chess.WHITE);
			}
			Future<Move> whiteMoveFuture = whiteEngine.getBestMove(gameContext);
			Move whiteMove = whiteMoveFuture.get();
			if (chessClock != null) {
				chessClock.stopClock();
				if (shouldAddInc) {
					chessClock.addTimeToClock(Chess.WHITE,engineMatchOptions.getInc()*1000);
				}
			}
			if (whiteMove != null) {
				game.getPosition().doMove(whiteMove.getShortValue());

				for(Runnable runnable : onEveryMoveHalfMoveRunnables) {
					runnable.run();
				}

				if (game.getPosition().isMate()) {
					gameResult = GameResult.WHITE_WIN;
					gameReason = "Black checkmated.";
					break;
				}
			}
			
			if (game.getPosition().isStaleMate()) {
				gameResult = GameResult.DRAW;
				gameReason = "Game drawn due to stalemate.";
				break;
			}
			if (ChesspressoUtils.isDraw(game.getPosition())) {
				gameResult = GameResult.DRAW;
				gameReason = "Game drawn.";
				break;
			}

			if (chessClock != null) {
				chessClock.startClock(Chess.BLACK);
			}
			Future<Move> blackMoveFuture = blackEngine.getBestMove(gameContext);
			Move blackMove = blackMoveFuture.get();
			if (chessClock != null) {
				chessClock.stopClock();
				if (shouldAddInc) {
					chessClock.addTimeToClock(Chess.BLACK,engineMatchOptions.getInc()*1000);
				}
			}
			if (blackMove != null) {
				game.getPosition().doMove(blackMove.getShortValue());

				for(Runnable runnable : onEveryMoveHalfMoveRunnables) {
					runnable.run();
				}

				if (game.getPosition().isMate()) {
					gameResult = GameResult.BLACK_WIN;
					gameReason = "White checkmated.";
					break;
				}
			}
		}
		
		game.setTag(PGN.TAG_RESULT,getResult(gameResult));
		engineMatchResult = new EngineMatchResult(game,gameResult,gameReason);
		return engineMatchResult;
	}
	
	private String getResult(GameResult gameResult) {
		if (gameResult == GameResult.WHITE_WIN) {
			return "1-0";
		} else if (gameResult == GameResult.DRAW) {
			return "1/2-1/2";
		} else if (gameResult == GameResult.BLACK_WIN) {
			return "0-1";
		}
		return null;
	}
}
