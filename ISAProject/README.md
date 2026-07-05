# İSA Programlama Dili

**Proje Adı:** İSA – Türkçe Temelli Mini Programlama Dili  
**Dil Adı:** İSA *(İCLAL – SUDE – AYŞE'nin baş harflerinden)*

---

## Grup Üyeleri

| İsim | Öğrenci No |
|------|-----------|
| İCLAL  | *(230315038)* |
| SUDE   | *(230315074)* |
| AYŞE   | *(230315020)* |

---

## Uygulama Dili

**Java** (JDK 11 ve üzeri)

---

## Projeyi Derleme

```bash
javac isa/ISA.java
```

Derleme sonucunda `ISA.class` dosyası oluşur.

---

## Projeyi Çalıştırma

```bash
java ISA <kaynak_dosya.isa>
```

**Örnek:**
```bash
java isa.ISA test1.isa
```

---

## Test Dosyaları

### Geçerli Test Dosyaları (4 adet)

| Dosya | İçerik |
|-------|--------|
| `test1.isa` | Değişken tanımlama ve atama ifadeleri |
| `test2.isa` | Aritmetik ifadeler `(+, -, *, /)` ve parantezli işlemler |
| `test3.isa` | Koşul yapısı: `ise` / `degilse` (if–else) |
| `test4.isa` | Döngü yapısı: `iken` (while) |

Tüm geçerli dosyaları sırayla çalıştırmak için:

```bash
java ISA test1.isa
java ISA test2.isa
java ISA test3.isa
java ISA test4.isa
```

### Geçersiz Test Dosyası (1 adet)

| Dosya | Hata Türü |
|-------|-----------|
| `test5_gecersiz.isa` | Sözdizimi hatası – `sayi x` satırında `;` eksik |

```bash
java ISA test5_gecersiz.isa
```

Beklenen çıktı: **SOZDIZIMI HATASI** mesajı ve programın durması.

---

## İSA Dili Anahtar Sözcükleri

| Türkçe Keyword | Karşılık |
|----------------|---------|
| `sayi`   | integer değişken bildirimi |
| `ise`    | if koşul ifadesi |
| `degilse`| else dalı |
| `iken`   | while döngüsü |
| `yazdir` | ekrana yazdırma |

---

## EBNF Grameri

```
<program>       → { <ifade> }
<ifade>         → <tanimlama> | <atama> | <kosul> | <dongu> | <yazdir>
<tanimlama>     → sayi [=] ;
<atama>         → <id> = <expr> ;
<kosul>         → ise ( <bool_expr> ) { <program> } [ degilse { <program> } ]
<dongu>         → iken ( <bool_expr> ) { <program> }
<yazdir>        → yazdir ( ) ;
<bool_expr>     → <expr> <rel_op>_op>        → == | != | < | > | <= | >=
<expr>          → <term> { (+ | -) <term> }
<term>          → <factor> { (* | /) <factor> }
<factor>        → <id> | <int> | ( <expr> )
<id>            → harf { harf | rakam }
<int>           → rakam { rakam }
```

---

## Dosya Listesi

```
ISA.java                  ← Kaynak kod (Lexer + Parser)
test1.isa                 ← Geçerli: değişken tanımlama & atama
test2.isa                 ← Geçerli: aritmetik ifadeler
test3.isa                 ← Geçerli: koşul ifadesi
test4.isa                 ← Geçerli: döngü yapısı
test5_gecersiz.isa        ← Geçersiz: sözdizimi hatası
README.md                 ← Bu dosya
```

---

## Temel Referans

Bu proje **Robert W. Sebesta – Concepts of Programming Languages (10. Baskı)**,  
**Bölüm 4: Lexical and Syntax Analysis** temel alınarak geliştirilmiştir.

- Sayfa 172–177: `front.c` lexical analyzer → `ISA.java` içindeki `lex()`, `getChar()`, `addChar()`, `getNonBlank()` fonksiyonlarına dönüştürüldü.  
- Sayfa 182–184: `expr()`, `term()`, `factor()` recursive-descent parser fonksiyonları doğrudan uyarlandı.  
- Eklenenler: Türkçe karakter desteği, `ise/degilse/iken/yazdir/sayi` anahtar sözcükleri, sembol tablosu, `<=` / `>=` / `==` / `!=` operatörleri, blok yapısı `{ }`, sözdizimi hata mesajları.
