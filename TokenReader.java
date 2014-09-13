import util.StreamReader;
import util.*;
import java.util.*;

/**
 * a skeleton TokenReader for Pascal tokens --
 * the only tokens returned are ERROR (for alphabetic characters)
 * and EOF
 */
public class TokenReader implements StreamReader<Token> {

    private LineIO lio;              // a StreamReader<Character>
    private Lazy<Character> lzin;   // a Lazy<Character> based on lio
    private static final char NL = '\n';    // the newline character
	Map<String, Token.Val> rwtab = new HashMap <String, Token.Val>();
    public TokenReader(LineIO lio) {
	this.lio = lio;
	lzin = new Lazy<Character>(lio);
	for(Token.Val val: EnumSet.range(Token.Val.AND, Token.Val.WITH)){
		rwtab.put(val.toString(), val);
	}
    }

    public Character cur() {
        return lzin.cur();
    }

    public void adv() {
        lzin.adv();
    }
	
	public void put(Character ch){
		lzin.put(ch);
	}

    public int lno() {
        return lio.getLineNumber();
    }
	
    // this variable is referenced in each FSMState method
    private Token tok;

    // the FSM states
	// commenting out this s_INIT, gonna writ it form scratch my way
/*
    private FSMState s_INIT = new FSMState() {
	    public FSMState next() {
		Character ch = cur();
		tok.lno = lno();
		if (ch == null) {
		    // end of file
		    tok.str.append("*EOF*");
		    tok.val = Token.Val.EOF;
		    return null;
		}
		adv();
		if (Character.isLetter(ch)) {
		    tok.str.append(ch);
		    tok.val = Token.Val.ERROR;
		    return  null;
		}
		// ignore anything else
		return s_INIT; // ... could loop instead ...
	    }
	};
*/
	private FSMState s_INIT = new FSMState(){
		public FSMState next() {
			Character ch = cur();
			tok.lno = lno();
		
			// covering nullification
			if(ch == null){
				tok.str.append("*EOF*");
				tok.val = Token.Val.EOF;
				return null;
			}
			// at this juncture, letters are still errors
			// this will be scrapped soon
			if(Character.isWhitespace(ch)){
				adv();
				return this;
			}	
			if(Character.isLetter(ch)){
				AA(ch);
				return s_Ident_reserv;
			}
			// dont break into the return init yet
			// cover actual cases first
			// known single char toekns
			// single tokens will return null here
			// doubles need to go deeper
			if(ch == '+'){
				AAV(ch, Token.Val.PLUS);
				return null;
			}
			if(ch == '-'){
				AAV(ch, Token.Val.MINUS);
				return null;
			}
			if(ch == '*'){
				AAV(ch, Token.Val.TIMES);
				return null;
			}
			if(ch == '/'){
				AAV(ch, Token.Val.DIVIDE);
				return null;
			}
			if(ch == '='){
				AAV(ch, Token.Val.EQU);
				return null;
			}
			if(ch == '('){
				AAV(ch, Token.Val.LPAREN);
				return null;
			}
			if(ch == ')'){
				AAV(ch, Token.Val.RPAREN);
				return null;
			}
			if(ch == '['){
				AAV(ch, Token.Val.LBRACK);
				return null;
			}
			if(ch == ']'){
				AAV(ch, Token.Val.RBRACK);
				return null;
			}
			if(ch == ','){
				AAV(ch, Token.Val.COMMA);
				return null;
			}
			if(ch == ';'){
				AAV(ch, Token.Val.SEMI);
				return null;
			}
			if(ch == '^'){
				AAV(ch, Token.Val.UPARROW);
				return null;
			}
    			// end of supply of single tokens.
			// now init will need to know to see the other tokens,
			// and will have to define the states for those
			
			if(ch == '<'){
				AA(ch);
				return s_LT;
			}
			if(ch == '>'){
				AA(ch);
				return s_GT;
			}
			if(ch == '.'){
				AA(ch);
				return s_DOT;
			}
			if(ch == ':'){
				AA(ch);
				return s_COL;
			}
			// strings go here
			if(ch == '\''){
				adv();
				return s_string;
			}
			if(Character.isDigit(ch)){
				AA(ch);
				return s_real_int;
			}
			// double symbol tokens tracked
			// now to throw in catch case
		//	System.out.ptintln("Woop woop, i dont think this adv");
			adv();	
			return s_INIT;
		}
	};
			
		// define other states here.
		// these states for the cases of multipul chatrs in the token
	private FSMState s_real_int = new FSMState(){
		public FSMState next(){
			Character ch = cur();
			tok.lno = lno();
			if(ch == '.'){
				adv();
				return s_real_not;
			}
			if(Character.isDigit(ch)){
				AA(ch);
				return this;
			}
			tok.val = Token.Val.INT;
			return null;
		}
	};
	private FSMState s_real_not = new FSMState(){
		public FSMState next(){
			Character ch = cur();
			tok.lno = lno();
			if(Character.isDigit(ch)){
				tok.str.append('.');
				AA(ch);
				return s_real;
			}
			put('.');
			tok.val = Token.Val.INT;
			return null;
		}
	};
	public FSMState s_real = new FSMState(){
		public FSMState next(){
			Character ch = cur();
			tok.lno = lno();
			if(Character.isDigit(ch)){
				AA(ch);
				return this;
			}
			tok.val = Token.Val.REAL;
			return null;
		}
	};
	private FSMState s_string = new FSMState(){
		public FSMState next(){
			Character ch = cur();
			tok.lno = lno();
			if(ch == '\''){
				adv();
				return s_sthap_string;
			}
			if(isPrint(ch)){
				AA(ch);
				return this;
			}
			tok.val = Token.Val.ESTRING;
			return null;
		}
	};
	private FSMState s_sthap_string = new FSMState(){
		public FSMState next(){
			Character ch = cur();
			tok.lno = lno();
			if(ch == '\''){
				AA(ch);
				return s_string;
			}
			tok.val = Token.Val.STRING;
			return null;
		}
	};

	private FSMState s_LT = new FSMState(){
		public FSMState next() {
			Character ch = cur();
			tok.lno = lno();
			if(ch == '>'){
				AAV(ch, Token.Val.NE);
				return null;
			}
			if(ch == '='){
				AAV(ch, Token.Val.LE);
				return null;
			}
			tok.val = Token.Val.LT;
			return null;
		}
	};
	private FSMState s_GT = new FSMState(){
		public FSMState next() {
			Character ch = cur();
			tok.lno = lno();
			if(ch == '='){
				AAV(ch, Token.Val.GE);
				return null;
			}
			tok.val = Token.Val.GT;
			return null;
		}
	};
	private FSMState s_DOT = new FSMState(){
		public FSMState next() {
			Character ch = cur();
			tok.lno = lno();
			if(ch == '.'){
				AAV(ch, Token.Val.DOTDOT);
				return null;
			}
			tok.val = Token.Val.DOT;
			return null;
		}
	};
	private FSMState s_COL = new FSMState(){
		public FSMState next(){
			Character ch = cur();
			tok.lno = lno();
			if(ch =='='){
				AAV(ch, Token.Val.ASSIGN);
				return null;
			}
			tok.val = Token.Val.COLON;
			return null;
		}
	};
	private FSMState s_Ident_reserv = new FSMState(){
		public FSMState next(){
			Character ch = cur();
			tok.lno = lno();
			if(Character.isLetter(ch) || Character.isDigit(ch) || ch == '_'){
				AA(ch);
				return s_Ident_reserv;
			}
			String rw = tok.str.toString().toUpperCase();
			Token.Val rval = rwtab.get(rw);
			if(rval == null){
				tok.val = Token.Val.ID;
				return null;
			}
			else{
				tok.val = rval;
				tok.str = new StringBuffer(rw);
				return null;
			}

		}
	};
	// that nshould theoretically finish the things for reading in single and double
	// symbol tokens
	
		
    public Token read() {
	tok = new Token(); // get a new Token object
	FSM.run(s_INIT); // initialize and run the FSM
	return tok;
    }
	// helper functions to deal with advance, append value
	// and advance append
	public void AAV(Character c, Token.Val v){
		adv();
		tok.str.append(c);
		tok.val = v;
	}
	public void AA(Character c){
		adv();
		tok.str.append(c);
	}
	
	public boolean isPrint(Character c){
		return (c != null) &&(' ' <= c) && (c <='~');
	}
}
