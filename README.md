# 🚀 İSA Programlama Dili

<p align="left">
  <img src="https://img.shields.io/badge/Language-Java-orange?style=flat-square&logo=java" alt="Java">
  <img src="https://img.shields.io/badge/Scope-Academic_Project-blue?style=flat-square" alt="Academic">
  <img src="https://img.shields.io/badge/Architecture-Lexer_%26_Parser-green?style=flat-square" alt="Architecture">
</p>

**İSA**, Türkçe sözdizimine (syntax) sahip, eğitim ve mini uygulamalar geliştirmek amacıyla tasarlanmış Türkçe tabanlı bir mini programlama dilidir. Projenin adı, geliştiricilerinin baş harflerinden oluşturulmuştur.

---

## 👥 Geliştiriciler (Grup Üyeleri)

| İsim | Öğrenci Numarası |
| :--- | :--- |
| **İclal** | `230315038` |
| **Sude** | `230315074` |
| **Ayşe** | `230315020` |

---

## 🛠️ Teknik Özellikler & Gereksinimler

- **Uygulama Dili:** Java (JDK 11 ve üzeri)
- **Mimari:** Sözcüksel Analiz (Lexer) & Sözdizimsel Analiz (Recursive-Descent Parser)
- **Temel Referans:** *Robert W. Sebesta – Concepts of Programming Languages (10th Edition)*, Chapter 4: Lexical and Syntax Analysis. 
  - Kitaptaki `front.c` yapısı genişletilerek `lex()`, `getChar()`, `addChar()`, `getNonBlank()` fonksiyonları Java'ya uyarlanmıştır.
  - Sayfa 182–184'teki `expr()`, `term()`, `factor()` recursive-descent parser mimarisi doğrudan entegre edilmiştir.
  - **Ek Özellikler:** Türkçe karakter desteği, `ise/degilse/iken/yazdir/sayi` anahtar sözcükleri (keywords), sembol tablosu yönetimi, gelişmiş operatörler (`<=`, `>=`, `==`, `!=`), süslü parantez `{ }` blok yapısı ve detaylı sözdizimi hata yakalama mekanizması.

---

## 📐 EBNF Grameri

İSA programlama dilinin biçimsel gramer yapısı (EBNF) aşağıdaki gibidir:

```ebnf
<program>       → { <ifade> }
<ifade>         → <tanimlama> | <atama> | <kosul> | <dongu> | <yazdir>
<tanimlama>     → sayi [=] ;
<atama>         → <id> = <expr> ;
<kosul>         → ise ( <bool_expr> ) { <program> } [ degilse { <program> } ]
<dongu>         → iken ( <bool_expr> ) { <program> }
<yazdir>        → yazdir ( ) ;
<bool_expr>     → <expr> <rel_op> <expr>
<rel_op>        → == | != | < | > | <= | >=
<expr>          → <term> { (+ | -) <term> }
<term>          → <factor> { (* | /) <factor> }
<factor>        → <id> | <int> | ( <expr> )
<id>            → harf { harf | rakam }
<int>           → rakam { rakam }
