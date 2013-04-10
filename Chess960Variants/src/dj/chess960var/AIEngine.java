package dj.chess960var;
import dj.chess960var.GameModel.pc;
import java.util.ArrayList;
import java.util.Random;

// For reference - positions:
//
//  dR dN dB dQ dK dB dN dR  =  0  1  2  3  4  5  6  7
//  dP dP dP dP dP dP dP dP  =  8  9  10 11 12 13 14 15
//  eS eS eS eS eS eS eS eS  =  16 17 18 19 20 21 22 23
//  eS eS eS eS eS eS eS eS  =  24 25 26 27 28 29 30 31
//  eS eS eS eS eS eS eS eS  =  32 33 34 35 36 37 38 39
//  eS eS eS eS eS eS eS eS  =  40 41 42 43 44 45 46 47
//  lP lP lP lP lP lP lP lP  =  48 49 50 51 52 53 54 55
//  lR lN lB lQ lK lB lN lR  =  56 57 58 59 60 61 62 63

public class AIEngine 
{
    // Used for convertMoveToString() method:
    private static String[] mSquares = 
        { "a8","b8","c8","d8","e8","f8","g8","h8",
          "a7","b7","c7","d7","e7","f7","g7","h7",
          "a6","b6","c6","d6","e6","f6","g6","h6",
          "a5","b5","c5","d5","e5","f5","g5","h5",
          "a4","b4","c4","d4","e4","f4","g4","h4",
          "a3","b3","c3","d3","e3","f3","g3","h3",
          "a2","b2","c2","d2","e2","f2","g2","h2",
          "a1","b1","c1","d1","e1","f1","g1","h1" };

    // Main public interface to the AIEngine class:
    public static String getNextMove( GameModel gameModel )
    {
        boolean isLightsTurn = gameModel.isLightsTurn();
        
        ArrayList<int[]> possibleMoves = getPossibleMoves( gameModel, isLightsTurn );
        
        // TODO: Factor this code with similar code in evaluateMove():

        int value;
        
        if( isLightsTurn )
            value = Integer.MIN_VALUE;
        else
            value = Integer.MAX_VALUE;
        
        int indexOfMoveToMake = -1;
        
        // Note: Since this is the root node of minimax, parameter lightToMove
        // (which tells us whose move it is at the root node) here is equal to
        // isLightsTurn

        for( int i = 0; i < possibleMoves.size(); i++ )
        {
            GameModel newGM = new GameModel(gameModel);
            
            int newValue = evaluateMove( newGM, 
                                         possibleMoves.get(i), 
                                         2,
                                         isLightsTurn );
            
            if( (isLightsTurn && newValue > value) ||
                (!isLightsTurn && newValue < value) )
            {
                value = newValue;
                indexOfMoveToMake = i;
            }
        }
        
        return convertMoveToString( possibleMoves.get(indexOfMoveToMake) );
    }
    
    private static ArrayList<int[]> getPossibleMoves( GameModel gameModel, 
                                                      boolean lightToMove )
    {
        boolean isLightsTurn = gameModel.isLightsTurn();
        
        ArrayList<int[]> possibleMoves = new ArrayList<int[]>(); 
        
        for( int oldPos = 0; oldPos < 64; oldPos++ )
        {
            // Create a list of possible moves in next two loops and sort
            // them according to some evaluation function and only keep
            // the top 10 or so:

            pc currPiece = gameModel.getPieceAtSquare(oldPos);
                
            if( (isLightsTurn && GameModel.isLightPiece(currPiece)) ||
                (!isLightsTurn && GameModel.isDarkPiece(currPiece)) )
            {
                for( int newPos = 0; newPos < 64; newPos++ )
                {                   
                    // Order of checks here is probably somewhat important;
                    // It's better to check the quicker verifiable things first
                    // to weed out any obviously invalid/poor moves:
                    
                    if( (oldPos != newPos)  &&
                        movePassesGeneralOpeningRules(gameModel,oldPos,newPos)
                        && gameModel.isValidMove(oldPos,newPos,false)
                        && movePassesGeneralRules(gameModel,oldPos,newPos))
                    {
                        int[] move = new int[3]; // {startPos,endPos,value}
                        move[0] = oldPos;
                        move[1] = newPos;
                        move[2] = -17; // For now, obscure value for testing
                                
                        possibleMoves.add(move);
                    }
                }
            }
        }
        
        // If it's our turn, we have control over which moves to filter out;
        // otherwise we can't trim off anymore moves; parameter lightToMove
        // indicates whether it was light's turn when the current iteration 
        // of minimax started, whereas isLightsTurn indicates whether we are 
        // at a max (true) or min (false) node of minimax:
        //
        if( isLightsTurn == lightToMove )
        {
            // Here we keep only 5 moves at random; so the minimax tree will 
            // have a branching factor of only 5:
            
            long mSeed = (new Random()).nextLong();
            Random randomizer = new Random(mSeed);
            
            while( possibleMoves.size() > 5 )
            {
                // nextInt(n) generates between 0 (inclusive) and n (exclusive):
                int indexToRemove = randomizer.nextInt(possibleMoves.size());
                possibleMoves.remove(indexToRemove);
            }
        }
        
        return possibleMoves;
    }
    
    private static boolean movePassesGeneralRules( 
        GameModel gameModel, int oldPos, int newPos )
    {
        // The main things we want to check here are that a piece is not being 
        // moved onto a square that is attacked by an opposing piece of lesser 
        // value (unless it captures a piece of equal or greater value first) 
        // and that we are not moving onto a square that is attacked by
        // opposing pieces more times than it is defended.
        
        // Note: A bishop is considered slightly more valuable than a knight,
        // but for these purposes, it should be considered the same.
        
        // Also note: we don't need to check anything checked by move
        // validation here (ie. if for example we tried to move an empty 
        // square), since that will all be checked after using isValidMove()
        
        // If move captures a piece of equal or greater value, none of the 
        // rules below applies:
        
        int valueOfMovingPiece = 
            getValueOfPiece( gameModel.getPieceAtSquare(oldPos) );
        int capturedValue = 
            getValueOfPiece( gameModel.getPieceAtSquare(newPos) );
        
        // +25 allows for bishop to capture knight:
        
        if( valueOfMovingPiece <= capturedValue + 25 )
            return true;
        
        GameModel newGM = new GameModel(gameModel);
        
        newGM.updateAfterMove( oldPos, newPos );
        
        // Here we are checking if after the move the moved piece can be 
        // immediately captured by an opposing piece of lesser value:
        
        for( int pos = 0; pos < 64; pos++ )
        {
            pc movedPiece = newGM.getPieceAtSquare(newPos);
            pc pieceAtCurrPos = newGM.getPieceAtSquare(pos);
            
            // If pieces are opposing and current piece can validly move onto
            // moved piece:
            //
            if( ( pieceAtCurrPos != pc.eS ) &&
                (GameModel.isLightPiece(pieceAtCurrPos) != 
                GameModel.isLightPiece(movedPiece)) &&
                newGM.isValidMove(pos,newPos,false) )
            {
                int currPieceValue = 
                    getValueOfPiece( gameModel.getPieceAtSquare(pos) );
                
                if( currPieceValue < valueOfMovingPiece - 25 )
                    return false;
            }
        }
        
        // TODO: Check we are not moving onto a square that is attacked by
        // opposing pieces more times than it is defended.
        
        return true;
    }
    
    private static boolean movePassesGeneralOpeningRules( 
        GameModel gameModel, int oldPos, int newPos )
    {
        boolean passesGeneralOpeningRules = false;
        
        pc capturedPiece = gameModel.getPieceAtSquare(newPos); // Can be eS
        
        // Here we enforce some basic rules that, during an opening, should 
        // hold for all moves other than a piece capture:
        // - OK to move queen's bishop pawn one or two spaces
        // - OK to move queen's pawn one or two spaces
        // - OK to move king's pawn one or two spaces
        // - OK to move either knight, except to the edge of the board
        // - OK to move either bishop
        // - OK to move queen's knight pawn or king's knight pawn one square
        // - OK to castle
        
        // Moves that are rarely made in the opening:
        // - Moving queen
        // - Moving king's bishop pawn
        // - Moving knight to edge of board
        // - Moving rooks (other than castling or sometimes to open files)
        
        if( pc.eS == capturedPiece )
        {
            // No piece is captured, so the above list of rules applies:
            
            pc movingPiece = gameModel.getPieceAtSquare(oldPos);
            
            switch( movingPiece )
            {
                case lP:
                    switch( newPos )
                    {
                        case 41:
                        case 42:
                        case 34:
                        case 43:
                        case 35:
                        case 44:                
                        case 36:
                        case 46:
                            passesGeneralOpeningRules = true;
                            break;
                        default:
                            break;
                    }
                    break;
                    
                case lN: // TODO: Add more squares for knights to move to (ie. moving twice)
                    switch( newPos )
                    {
                        case 42:
                        case 51:
                        case 52:
                        case 45:
                            passesGeneralOpeningRules = true;
                            break;
                        default:
                            break;
                    }
                    break;
                    
                case lB:
                    passesGeneralOpeningRules = true;
                    break;
                    
                case lK:
                    if( pc.lR == capturedPiece ) // Castling
                        passesGeneralOpeningRules = true;
                    break;
                    
                case dP:
                    switch( newPos )
                    {
                        case 17:
                        case 18:
                        case 26:
                        case 19:
                        case 27:
                        case 20:                
                        case 28:
                        case 22:
                            passesGeneralOpeningRules = true;
                            break;
                        default:
                            break;
                    }
                    break;
                    
                case dN: // TODO: Add more squares for knights to move to (ie. moving twice)
                    switch( newPos )
                    {
                        case 18:
                        case 11:
                        case 12:
                        case 21:
                            passesGeneralOpeningRules = true;
                            break;
                        default:
                            break;
                    }
                    break;
                    
                case dB:
                    passesGeneralOpeningRules = true;
                    break;
                    
                case dK:
                    if( pc.dR == capturedPiece ) // Castling
                        passesGeneralOpeningRules = true;
                    break;
                    
                default:
                    passesGeneralOpeningRules = false;
            }
        }
        
        return passesGeneralOpeningRules;
    }
    
    private static int evaluateMove( GameModel gameModel,
                                     int[] theMove,
                                     int depth,
                                     boolean lightToMove )
    {
        // Assumption: we only ever care to evaluate a move if it is our turn
        // (so we will never perform an evaulation for white when it is black's
        // turn and vice-versa).
        
        // For now, just do minimax without alpa-beta pruning
        // TODO: Add alpha-beta pruning
        
        GameModel newGM = new GameModel(gameModel);
        
        newGM.updateAfterMove( theMove[0], theMove[1] );
        
        int value;
        
        if( 0 == depth )
        {        
            value = evaluatePosition( newGM );
        }
        else
        {
            // Set value to minimum value over all possible moves after 
            // "theMove":
            
            boolean isLightsTurn = newGM.isLightsTurn();
            
            if( isLightsTurn )
                value = Integer.MIN_VALUE;
            else
                value = Integer.MAX_VALUE;
            
            ArrayList<int[]> possibleMoves = getPossibleMoves( newGM, 
                                                               lightToMove );
            
            for( int i = 0; i < possibleMoves.size(); i++ )
            {
                int newValue = evaluateMove( newGM, 
                                             possibleMoves.get(i), 
                                             depth-1,
                                             lightToMove );
                
                if( (isLightsTurn && newValue > value) ||
                    (!isLightsTurn && newValue < value) )
                        value = newValue;
            }
        }
        
        return value;
    }

    private static int evaluatePosition( GameModel gameModel )
    {
        // Given a representation of the board, where pieces are on the
        // board and whose move it is, this function generates a "utility"
        // for that state in the game.  This utility should be a measure of
        // how good the given position is for white (so white wants to
        // maximize these values and black wants to minimize).  It
        // can be positive or negative.
        
        pc[][] board_rep = gameModel.getBoardRep();
        
        // We want to check for basic things here like:
        //
        //  (1) does one side have a material advantage and the next more; 
        // if so, how much is the material advantage?
        // 
        //  (2) some measure of how many squares in the center are occupied/
        // attacked
        
        int lightsScore = getLightsMaterial(board_rep);
        int darksScore = getDarksMaterial(board_rep);
        
        // http://www.gamedev.net/page/resources/_/technical/
        // artificial-intelligence/chess-programming-part-vi-evaluation-
        // functions-r1208 :
        //
        // "Yet, it [material balance] is by far the overwhelming factor in any
        // chess board evaluation function. CHESS 4.5's creators estimate that 
        // an enormous advantage in position, mobility and safety is worth less 
        // than 1.5 pawns."
        
        // And so we should weight a decisive advantage in control of the 
        // center (for opening) as about 125 points:
        
        // Count light and dark pieces occupying and/or controlling the center
        // (c3 to f6)
        
        int numberOfLightsInCenter = 0;
        int numberOfDarksInCenter = 0;
        
        for( int i = 2; i < 5; i++ ) // rows 3 to 6
            for( int j = 2; j < 5; j++ ) // columns c to f
                if( GameModel.isLightPiece(board_rep[i][j]) )
                    numberOfLightsInCenter++;
                else if( GameModel.isDarkPiece(board_rep[i][j]) )
                    numberOfDarksInCenter++;
                else {}
                    // Do nothing
                    
        if( numberOfLightsInCenter - numberOfDarksInCenter >= 2 )
            lightsScore += 125;
        else if( numberOfDarksInCenter - numberOfLightsInCenter >= 2 )
            darksScore += 125;
        else {}
            // Do nothing
        
        // TODO: Factor in piece development; light should be penalized for
        // being behind in development by one move, and black should
        // be penalized for being behind in development by two moves.

        return lightsScore - darksScore;
    }
    
    private static int getLightsMaterial( pc[][] board_rep )
    {
        int lightsMaterial = 0;
        
        // TODO: add an infinite value for the king and incorporate that into 
        // the minimax search (so that any check mate will result in a capture
        // of the king and result in infinite value for the opposing side).
        // For now we assume that game remains in opening stages and a 
        // check mate is not possible.
        
        for( int i = 0; i < board_rep.length; i++ )
        {
            for( int j = 0; j < board_rep[0].length; j++ )
            {
                if( GameModel.isLightPiece(board_rep[i][j]) )
                    lightsMaterial += getValueOfPiece( board_rep[i][j] );
            }
        }
        
        return lightsMaterial;
    }
    
    private static int getDarksMaterial( pc[][] board_rep )
    {
        int darksMaterial = 0;
        
        // TODO: add an infinite value for the king and incorporate that into 
        // the minimax search (so that any check mate will result in a capture
        // of the king and result in infinite value for the opposing side).
        // For now we assume that game remains in opening stages and a 
        // check mate is not possible.
        
        for( int i = 0; i < board_rep.length; i++ )
        {
            for( int j = 0; j < board_rep[0].length; j++ )
            {
                if( GameModel.isDarkPiece(board_rep[i][j]) )
                    darksMaterial += getValueOfPiece( board_rep[i][j] );
            }
        }
        
        return darksMaterial;
    }
    
    private static int getValueOfPiece( pc thePiece )
    {
        int value = 0;
        
        switch( thePiece )
        {
        case eS:
        case lK:
        case dK:
            // Do nothing - leave value at zero
            break;
        case lP:
        case dP:
            value = 100;
            break;
        case lN:
        case dN:
            value = 300;
            break;
        case lB:
        case dB:
            value = 325;
            break;
        case lR:
        case dR:
            value = 500;
            break;
        case lQ:
        case dQ:
            value = 900;
            break;
        default:
            throw new AssertionError("Invalid piece");
        }
        
        return value;
    }
    
    private static String convertMoveToString( int[] theMove )
    {
        // Moves are represented as arrays of ints, where the first element
        // is the start position of the move and the second element is the
        // ending position of the move.
        
        String moveAsString =
                mSquares[theMove[0]] + " to " + mSquares[theMove[1]];
        
        return moveAsString;
    }
}
