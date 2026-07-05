package isa;

/**
 * ISA.java
 * ─────────────────────────────────────────────────────────────────────────────
 *  Dil adı : İSA  (İCLAL – SUDE – AYŞE'nin baş harflerinden)
 *
 *  Türkçe anahtar sözcükler:
 *      sayi        → integer variable declaration  (sayi x; veya sayi x = 5;)
 *      ise         → if
 *      degilse     → else
 *      iken        → while
 *      yazdir      → print  (yazdir( <expr> );)
 *
 *
 *  Derleme    : javac ISA.java
 *  Çalıştırma : java ISA <kaynak_dosya.isa>
 *
 *  Orijinal koddan farklar / eklemeler:
 *    1. Yorum satırı (//) desteği – getNonBlank() içinde temiz şekilde atlanır
 *    2. Satır numarası takibi – tüm hata mesajları satır no gösterir
 *    3. Sembol tablosu ile bildirilmemiş değişken kontrolü – factor() içinde
 *    4. yazdir() artık tam <expr> kabul ediyor (sadece tek id/int değil)
 *    5. Bildirimde başlangıç değeri: sayi x = 5;  biçimi destekleniyor
 *    6. <= operatörü boolExpr() içindeki token tüketim sırası hatası düzeltildi
 * ─────────────────────────────────────────────────────────────────────────────
 */

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

// ─── TOKEN TANIMLARI ────────────────────────────────────────────────────────
enum Token {
    // Anahtar kelimeler
    SAYI_CODE,          // sayi  (int declaration)
    ISE_CODE,           // ise   (if)
    DEGILSE_CODE,       // degilse (else)
    IKEN_CODE,          // iken  (while)
    YAZDIR_CODE,        // yazdir (print)

    // Tanımlayıcı ve sayı sabiti
    IDENT_CODE,
    INT_LIT_CODE,

    // Aritmetik operatörler
    ADD_OP,             // +
    SUB_OP,             // -
    MULT_OP,            // *
    DIV_OP,             // /

    // Atama ve ilişkisel operatörler
    ASSIGN_OP,          // =
    EQUAL_OP,           // ==
    NOT_EQUAL_OP,       // !=
    LESS_OP,            // <
    GREATER_OP,         // >
    LESS_EQUAL_OP,      // <=
    GREATER_EQUAL_OP,   // >=

    // Ayırıcılar
    LEFT_PAREN,         // (
    RIGHT_PAREN,        // )
    LEFT_BRACE,         // {
    RIGHT_BRACE,        // }
    SEMICOLON,          // ;

    // Dosya sonu
    EOF_CODE
}

// ─── ANA SINIF ───────────────────────────────────────────────────────────────
public class ISA {

    // ── Kitaptaki global değişkenlerin Java karşılıkları ──
    static int        charClass;
    static char[]     lexeme  = new char[100];
    static char       nextChar;
    static int        lexLen;
    static Token      nextToken;
    static FileReader reader;

    // Karakter sınıfları (kitaptaki #define sabitlerinin karşılığı)
    static final int LETTER    = 0;
    static final int DIGIT     = 1;
    static final int UNKNOWN   = 99;
    static final int EOF_CLASS = -1;

    // ── Satır numarası takibi ────────────────────────────────────────
    static int satirNo = 1;

    // ── Sembol / Arama tablosu ────────────────────────────────────
    static HashSet<String> sembolTablosu = new HashSet<>();

    // ════════════════════════════════════════════════════════════════════════
    //  MAIN
    // ════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Kullanim: java ISA <kaynak_dosya.isa>");
            return;
        }
        try {
            reader = new FileReader(args[0]);
            System.out.println("═══════════════════════════════════════════");
            System.out.println("  İSA Dili Çözümleyici Başlatılıyor");
            System.out.println("  Dosya: " + args[0]);
            System.out.println("═══════════════════════════════════════════\n");

            getChar();   // İlk karakteri oku
            program();   // Sözdizimi analizini başlat

            System.out.println("\n═══════════════════════════════════════════");
            System.out.println("  BAŞARILI: Kod gramere tam uygun.");
            System.out.println("  Sembol Tablosu: " + sembolTablosu);
            System.out.println("═══════════════════════════════════════════");
            reader.close();
        } catch (IOException e) {
            System.out.println("Dosya okuma hatasi: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BÖLÜM 5 – LEXİKAL ANALİZÖR
    // ════════════════════════════════════════════════════════════════════════

    /**
     * getChar() – Kitaptaki getChar() fonksiyonunun Java karşılığı
     * Bir sonraki karakteri okur, karakter sınıfını belirler.
     * Türkçe harfler (ç,ğ,ı,ö,ş,ü ve büyükleri) LETTER sınıfına dahildir.
     */
    static void getChar() {
        try {
            int c = reader.read();
            if (c != -1) {
                nextChar = (char) c;
                if (nextChar == '\n') satirNo++;

                if (isTurkishLetter(nextChar)) {
                    charClass = LETTER;
                } else if (Character.isDigit(nextChar)) {
                    charClass = DIGIT;
                } else {
                    charClass = UNKNOWN;
                }
            } else {
                charClass = EOF_CLASS;
            }
        } catch (IOException e) {
            charClass = EOF_CLASS;
        }
    }

    /** Türkçe dahil harfleri tanıyan yardımcı metod */
    static boolean isTurkishLetter(char c) {
        return Character.isLetter(c)
                || c == 'ç' || c == 'Ç'
                || c == 'ğ' || c == 'Ğ'
                || c == 'ı' || c == 'İ'
                || c == 'ö' || c == 'Ö'
                || c == 'ş' || c == 'Ş'
                || c == 'ü' || c == 'Ü';
    }

    /**
     * addChar() – Kitaptaki addChar() fonksiyonu
     * nextChar'ı lexeme dizisine ekler.
     */
    static void addChar() {
        if (lexLen <= 98) {
            lexeme[lexLen++] = nextChar;
            lexeme[lexLen]   = 0;
        } else {
            System.out.println("Hata: Lexeme cok uzun (satir " + satirNo + ").");
        }
    }

    /**
     * getNonBlank() – Kitaptaki getNonBlank() fonksiyonu
     * Boşlukları atlar VE '//' ile başlayan yorum satırlarını temizler.
     */
    static void getNonBlank() {
        // Boşluk veya yorum satırı olduğu sürece döngüde kal
        while (true) {
            // Boşlukları atla
            while (charClass != EOF_CLASS && Character.isWhitespace(nextChar)) {
                getChar();
            }
            // Yorum satırı kontrolü – '//' ise satır sonuna kadar atla
            if (charClass == UNKNOWN && nextChar == '/') {
                // Bir sonraki karaktere ihtiyaç var; onu peek etmek için okuyoruz.
                // ama geri alamayız, bu yüzden geçici olarak saklıyoruz.
                // Yorum değilse DIV_OP için nextChar'ı '/' olarak bırakmak yeterli.
                // Bu yüzden reader.mark/reset kullanmak yerine bir bayrak tutuyoruz:
                // Eğer sonraki char yine '/' ise yorum, değilse dur.
                char savedChar = nextChar;
                int  savedClass = charClass;
                getChar(); // ikinci karakteri oku
                if (nextChar == '/') {
                    // Yorum satırı: satır sonuna kadar atla
                    while (charClass != EOF_CLASS && nextChar != '\n') {
                        getChar();
                    }
                    // Döngünün başına dön; sonraki satırdaki boşlukları da atla
                } else {
                    // Gerçek bir '/' operatörü – okuduğumuz ikinci char'ı "geri koy"
                    // Bunu yapamayız doğrudan, bu yüzden nextChar/charClass'ı
                    // geri yükleyip döngüden çıkıyoruz.
                    // Çözüm: savedChar ve savedClass'ı geri yükle, ikinci char'ı
                    // lookahead olarak tut (lex() içindeki UNKNOWN case'i bunu okur).
                    // Ancak bu iki karakterli durumu lex() içinde çözmek daha temiz;
                    // bu yüzden burada nextChar'ı sıfırlayıp işi lex()'e bırakıyoruz.
                    // Mevcut nextChar ikinci char olarak lex()'in UNKNOWN dalında kullanılır.
                    // '/' tokenını işaretlemek için önce nextChar'ı savedChar yap:
                    // ** Bu yaklaşım çift-karakter lookahead gerektiriyor.
                    // En temiz çözüm: ikinci karakteri pendingChar'a kaydetmek. **
                    pendingChar      = nextChar;
                    pendingCharClass = charClass;
                    hasPending       = true;
                    nextChar         = savedChar;
                    charClass        = savedClass;
                    return; // Döngüden çık; lex() '/' i işleyecek
                }
            } else {
                break; // Ne boşluk ne yorum; gerçek bir token başlıyor
            }
        }
    }

    // ── Tek karakterlik "geri koy" tamponu (getNonBlank lookahead için) ────
    static char    pendingChar      = 0;
    static int     pendingCharClass = UNKNOWN;
    static boolean hasPending       = false;

    /**
     * getCharOrPending() – Eğer getNonBlank() bir karakteri geri koymuşsa
     * onu döndürür, yoksa getChar()'ı çağırır.
     * lex() içinde UNKNOWN dalı bu metodu kullanarak ikinci karakteri alır.
     */
    static void consumePending() {
        if (hasPending) {
            nextChar      = pendingChar;
            charClass     = pendingCharClass;
            hasPending    = false;
        } else {
            getChar();
        }
    }

    /**
     * kelimeTurunuBul() – Kitaptaki lookup() fonksiyonunun anahtar-kelime
     * karşılığı, okunan kelimeyi arama tablosunda kontrol eder.
     */
    static Token kelimeTurunuBul(String kelime) {
        switch (kelime) {
            case "sayi":     return Token.SAYI_CODE;
            case "ise":      return Token.ISE_CODE;
            case "degilse":  return Token.DEGILSE_CODE;
            case "iken":     return Token.IKEN_CODE;
            case "yazdir":   return Token.YAZDIR_CODE;
            default:         return Token.IDENT_CODE;
        }
    }

    /**
     * lex() – Kitaptaki lex() fonksiyonu
     * Bir sonraki lexeme'i döndürür; token kodunu nextToken'a yazar.
     */
    static void lex() {
        lexLen = 0;
        getNonBlank();

        switch (charClass) {

            // ── Tanımlayıcı ve anahtar kelimeler ─────────
            case LETTER:
                addChar();
                getChar();
                while (charClass == LETTER || charClass == DIGIT) {
                    addChar();
                    getChar();
                }
                String okunan = new String(lexeme, 0, lexLen);
                nextToken = kelimeTurunuBul(okunan);
                break;

            // ── Tam sayı sabiti ──────────────────────────
            case DIGIT:
                addChar();
                getChar();
                while (charClass == DIGIT) {
                    addChar();
                    getChar();
                }
                nextToken = Token.INT_LIT_CODE;
                break;

            // ── Operatörler ve ayırıcılar
            case UNKNOWN:
                switch (nextChar) {
                    case '+': addChar(); getChar(); nextToken = Token.ADD_OP;      break;
                    case '-': addChar(); getChar(); nextToken = Token.SUB_OP;      break;
                    case '*': addChar(); getChar(); nextToken = Token.MULT_OP;     break;
                    case '(': addChar(); getChar(); nextToken = Token.LEFT_PAREN;  break;
                    case ')': addChar(); getChar(); nextToken = Token.RIGHT_PAREN; break;
                    case '{': addChar(); getChar(); nextToken = Token.LEFT_BRACE;  break;
                    case '}': addChar(); getChar(); nextToken = Token.RIGHT_BRACE; break;
                    case ';': addChar(); getChar(); nextToken = Token.SEMICOLON;   break;


                    case '/':
                        addChar();
                        consumePending(); // pendingChar varsa kullan, yoksa getChar()
                        nextToken = Token.DIV_OP;
                        break;

                    case '=':
                        addChar(); getChar();
                        if (nextChar == '=') { addChar(); getChar(); nextToken = Token.EQUAL_OP; }
                        else                 { nextToken = Token.ASSIGN_OP; }
                        break;
                    case '!':
                        addChar(); getChar();
                        if (nextChar == '=') { addChar(); getChar(); nextToken = Token.NOT_EQUAL_OP; }
                        else { lexikalHata("'!' sonrasi '=' bekleniyor"); nextToken = Token.EOF_CODE; }
                        break;

                    case '<':
                        addChar(); getChar();
                        if (nextChar == '=') { addChar(); getChar(); nextToken = Token.LESS_EQUAL_OP; }
                        else                 { nextToken = Token.LESS_OP; }
                        break;
                    case '>':
                        addChar(); getChar();
                        if (nextChar == '=') { addChar(); getChar(); nextToken = Token.GREATER_EQUAL_OP; }
                        else                 { nextToken = Token.GREATER_OP; }
                        break;

                    default:
                        lexikalHata("Tanimlanmayan karakter: '" + nextChar + "'");
                        addChar(); getChar();
                        nextToken = Token.EOF_CODE;
                        break;
                }
                break;

            // ── Dosya sonu ────────────────────────────────
            case EOF_CLASS:
                nextToken  = Token.EOF_CODE;
                lexeme[0]  = 'E'; lexeme[1] = 'O'; lexeme[2] = 'F'; lexeme[3] = 0;
                lexLen = 3;
                break;
        }

        printToken();
    }

    static void printToken() {
        System.out.printf("  [LEX] Satir %-3d | Token: %-20s | Lexeme: %s%n",
                satirNo, nextToken, new String(lexeme, 0, lexLen));
    }

    static void lexikalHata(String mesaj) {
        // FIX 2: satır numarası eklendi
        System.err.println("LEKSIK HATA (Satir " + satirNo + "): " + mesaj);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BÖLÜM 6 – SÖZDIZIMI ANALİZÖRÜ / RECURSIVE-DESCENT PARSER
    // ════════════════════════════════════════════════════════════════════════

    /** program() – <program> → { <ifade> } */
    static void program() {
        System.out.println("[PARSER] Sozdizimi analizi basliyor...\n");
        lex();          // Kitabın main()'indeki gibi ilk token'ı al (s.184)
        ifadeListesi();
    }

    /** ifadeListesi() – <ifade_listesi> → { <ifade> } */
    static void ifadeListesi() {
        while (nextToken != Token.EOF_CODE && nextToken != Token.RIGHT_BRACE) {
            ifade();
        }
    }

    /**
     * ifade() – Hangi yapının geleceğini belirler; ilgili fonksiyona yönlendirir.
     * <ifade> → <tanimlama> | <atama> | <kosul> | <dongu> | <yazdir>
     */
    static void ifade() {
        System.out.println("[PARSER] Giris <ifade>");
        if      (nextToken == Token.SAYI_CODE)   tanimlama();
        else if (nextToken == Token.IDENT_CODE)  atama();
        else if (nextToken == Token.ISE_CODE)    kosulIfadesi();
        else if (nextToken == Token.IKEN_CODE)   donguIfadesi();
        else if (nextToken == Token.YAZDIR_CODE) yazdirIfadesi();
        else    sozdizimHatasi("Gecersiz ifade baslangici: " + nextToken);
        System.out.println("[PARSER] Cikis <ifade>\n");
    }

    /**
     * tanimlama() – Değişken bildirimi.
     * FIX 5: sayi x;  VEYA  sayi x = <expr>;  her ikisi de destekleniyor.
     * <tanimlama> → sayi <id> [ = <expr> ] ;
     */
    static void tanimlama() {
        System.out.println("[PARSER]   Giris <tanimlama>");
        lex(); // 'sayi' kelimesini geç
        if (nextToken != Token.IDENT_CODE) {
            sozdizimHatasi("'sayi' sonrasi degisken ismi bekleniyor.");
        }

        String ad = new String(lexeme, 0, lexLen);
        sembolTablosu.add(ad);
        System.out.println("[SEMBOL]   Tabloya eklendi: '" + ad + "'");
        lex();

        if (nextToken == Token.ASSIGN_OP) {
            lex();
            expr();
        }

        if (nextToken == Token.SEMICOLON) {
            lex();
        } else {
            sozdizimHatasi("Tanimlama sonunda ';' bekleniyor.");
        }
        System.out.println("[PARSER]   Cikis <tanimlama>");
    }


    // atama() – Atama ifadesi.

    static void atama() {
        System.out.println("[PARSER]   Giris <atama>");
        String ad = new String(lexeme, 0, lexLen);
        if (!sembolTablosu.contains(ad)) {
            sozdizimHatasi("Bildirilmemis degisken kullanimi: '" + ad + "'");
        }
        lex(); // identifier'ı geç
        if (nextToken != Token.ASSIGN_OP) {
            sozdizimHatasi("Atama icin '=' bekleniyor.");
        }
        lex(); // '=' geç
        expr();
        if (nextToken == Token.SEMICOLON) {
            lex();
        } else {
            sozdizimHatasi("Atama sonunda ';' bekleniyor.");
        }
        System.out.println("[PARSER]   Cikis <atama>");
    }


    static void yazdirIfadesi() {
        System.out.println("[PARSER]   Giris <yazdir_ifadesi>");
        lex(); // 'yazdir' geç
        if (nextToken != Token.LEFT_PAREN) {
            sozdizimHatasi("yazdir sonrasi '(' bekleniyor.");
        }
        lex(); // '(' geç
        expr(); // herhangi bir aritmetik ifadeyi kabul et
        if (nextToken != Token.RIGHT_PAREN) {
            sozdizimHatasi("yazdir icinde kapanis ')' bekleniyor.");
        }
        lex(); // ')' geç
        if (nextToken != Token.SEMICOLON) {
            sozdizimHatasi("yazdir sonunda ';' bekleniyor.");
        }
        lex(); // ';' geç
        System.out.println("[PARSER]   Cikis <yazdir_ifadesi>");
    }

    /**
     * kosulIfadesi() – If-else yapısı.
     * <kosul> → ise ( <bool_expr> ) { <ifade_listesi> } [ degilse { <ifade_listesi> } ]
     */
    static void kosulIfadesi() {
        System.out.println("[PARSER]   Giris <kosul_ifadesi>");
        lex(); // 'ise' geç
        if (nextToken != Token.LEFT_PAREN)  sozdizimHatasi("ise sonrasi '(' bekleniyor.");
        lex();
        boolExpr();
        if (nextToken != Token.RIGHT_PAREN) sozdizimHatasi("Kosul kapanis ')' bekleniyor.");
        lex();
        if (nextToken != Token.LEFT_BRACE)  sozdizimHatasi("ise blogunu acmak icin '{' bekleniyor.");
        lex();
        ifadeListesi();
        if (nextToken != Token.RIGHT_BRACE) sozdizimHatasi("ise blogunu kapatmak icin '}' bekleniyor.");
        lex();
        if (nextToken == Token.DEGILSE_CODE) {
            System.out.println("[PARSER]   Giris <degilse>");
            lex();
            if (nextToken != Token.LEFT_BRACE)  sozdizimHatasi("degilse blogunu acmak icin '{' bekleniyor.");
            lex();
            ifadeListesi();
            if (nextToken != Token.RIGHT_BRACE) sozdizimHatasi("degilse blogunu kapatmak icin '}' bekleniyor.");
            lex();
            System.out.println("[PARSER]   Cikis <degilse>");
        }
        System.out.println("[PARSER]   Cikis <kosul_ifadesi>");
    }

    /**
     * donguIfadesi() – While döngüsü.
     * <dongu> → iken ( <bool_expr> ) { <ifade_listesi> }
     */
    static void donguIfadesi() {
        System.out.println("[PARSER]   Giris <dongu_ifadesi>");
        lex(); // 'iken' geç
        if (nextToken != Token.LEFT_PAREN)  sozdizimHatasi("iken sonrasi '(' bekleniyor.");
        lex();
        boolExpr();
        if (nextToken != Token.RIGHT_PAREN) sozdizimHatasi("Dongu kapanis ')' bekleniyor.");
        lex();
        if (nextToken != Token.LEFT_BRACE)  sozdizimHatasi("iken blogunu acmak icin '{' bekleniyor.");
        lex();
        ifadeListesi();
        if (nextToken != Token.RIGHT_BRACE) sozdizimHatasi("iken blogunu kapatmak icin '}' bekleniyor.");
        lex();
        System.out.println("[PARSER]   Cikis <dongu_ifadesi>");
    }


    // boolExpr() – İlişkisel karşılaştırma ifadesi.

    static void boolExpr() {
        System.out.println("[PARSER]     Giris <bool_expr>");
        expr();
        if (nextToken == Token.LESS_OP       || nextToken == Token.GREATER_OP   ||
                nextToken == Token.LESS_EQUAL_OP || nextToken == Token.GREATER_EQUAL_OP ||
                nextToken == Token.EQUAL_OP      || nextToken == Token.NOT_EQUAL_OP) {
            lex();
            expr();
        } else {
            sozdizimHatasi("Iliskisel operator bekleniyor (==, !=, <, >, <=, >=).");
        }
        System.out.println("[PARSER]     Cikis <bool_expr>");
    }

    // ── Aritmetik ifadeler ─────────

    /**
     * expr() – Kitaptaki expr() fonksiyonu
     * <expr> → <term> { (+ | -) <term> }
     */
    static void expr() {
        System.out.println("[PARSER]     Giris <expr>");
        term();
        while (nextToken == Token.ADD_OP || nextToken == Token.SUB_OP) {
            lex();
            term();
        }
        System.out.println("[PARSER]     Cikis <expr>");
    }

    /**
     * term() – Kitaptaki term() fonksiyonu
     * <term> → <factor> { (* | /) <factor> }
     */
    static void term() {
        System.out.println("[PARSER]     Giris <term>");
        factor();
        while (nextToken == Token.MULT_OP || nextToken == Token.DIV_OP) {
            lex();
            factor();
        }
        System.out.println("[PARSER]     Cikis <term>");
    }


    // factor() – Kitaptaki factor() fonksiyonu

    static void factor() {
        System.out.println("[PARSER]     Giris <factor>");
        if (nextToken == Token.IDENT_CODE) {
            // FIX 3: bildirilmemiş değişken kullanım kontrolü
            String ad = new String(lexeme, 0, lexLen);
            if (!sembolTablosu.contains(ad)) {
                sozdizimHatasi("Bildirilmemis degisken kullanimi: '" + ad + "'");
            }
            lex();
        } else if (nextToken == Token.INT_LIT_CODE) {
            lex();
        } else if (nextToken == Token.LEFT_PAREN) {
            lex();
            expr();
            if (nextToken == Token.RIGHT_PAREN) {
                lex();
            } else {
                sozdizimHatasi("Aritmetik ifadede kapanis ')' bekleniyor.");
            }
        } else {
            sozdizimHatasi("Sayi, degisken ya da '(' bekleniyor.");
        }
        System.out.println("[PARSER]     Cikis <factor>");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HATA YÖNETİMİ
    // ════════════════════════════════════════════════════════════════════════


    // sozdizimHatasi()

    static void sozdizimHatasi(String mesaj) {
        System.err.println();
        System.err.println("╔══════════════════════════════════════════════════╗");
        System.err.println("║  SOZDIZIMI HATASI (Syntax Error)                 ║");
        System.err.printf( "║  Satir     : %-35d ║%n", satirNo);
        System.err.printf( "║  Mesaj     : %-35s ║%n", mesaj);
        System.err.printf( "║  Token     : %-35s ║%n", nextToken);
        System.err.printf( "║  Lexeme    : %-35s ║%n", new String(lexeme, 0, lexLen));
        System.err.println("╚══════════════════════════════════════════════════╝");
        System.exit(1);
    }
}