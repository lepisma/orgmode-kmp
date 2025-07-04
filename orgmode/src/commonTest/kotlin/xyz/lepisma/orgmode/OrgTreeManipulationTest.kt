package xyz.lepisma.orgmode

import io.kotest.core.spec.style.StringSpec
import xyz.lepisma.orgmode.lexer.OrgLexer

const val orgTreeManipulationTestText = """:PROPERTIES:
:ID:      21e2c8f6-8dbb-4002-bcf5-a15203516114
:END:
#+TITLE: Org Test
#+PILE: pinned:t
#+TOC: headlines 2

jello world

#+BEGIN_page-intro
This is an introductory paragraph. Kind of like an abstract. Let's fill this a
little. Pellentesque dapibus suscipit ligula. Donec posuere augue in quam. Etiam
vel tortor sodales tellus ultricies commodo. Suspendisse potenti. Aenean in sem
ac leo mollis blandit. Donec neque quam, dignissim in, mollis nec, sagittis eu,
wisi. Phasellus lacus. Etiam laoreet quam sed arcu. Phasellus at dui in ligula
mollis ultricies. Integer placerat tristique nisl.
#+END_page-intro

This is a test file with most of the features of org mode that I use. Its
purpose is to test the export and fix styling issues, if any. Nullam eu ante vel
est convallis dignissim.  Fusce suscipit, wisi nec facilisis facilisis, est dui
fermentum leo, quis tempor ligula erat quis odio.  Nunc porta vulputate tellus.
Nunc rutrum turpis sed pede.  Sed bibendum.  Aliquam posuere.  Nunc aliquet,
augue nec adipiscing interdum, lacus tellus malesuada massa, quis varius mi
purus non odio.  Pellentesque condimentum, magna ut suscipit hendrerit, ipsum
augue ornare nulla, non luctus diam neque sit amet urna.  Curabitur vulputate
vestibulum lorem.  Fusce sagittis, libero non molestie mollis, magna orci
ultrices dolor, at vulputate neque nulla lacinia eros.  Sed id ligula quis est
convallis tempor.  Curabitur lacinia pulvinar nibh.  Nam a sapien.

* Heading level 1
** Heading level 2
*** Heading level 3
**** Heading level 4
Some checkboxes follow
- [ ] Lol
- [X] This is done
- [ ] Kek

More testing for lists

- Nullam eu ante vel est convallis dignissim.  Fusce suscipit, wisi nec
  facilisis facilisis, est dui fermentum leo, quis tempor ligula erat quis odio.
  Nunc porta vulputate tellus.  Nunc rutrum turpis sed pede.  Sed bibendum.
  Aliquam posuere.  Nunc aliquet, augue nec adipiscing interdum, lacus tellus
  malesuada massa, quis varius mi purus non odio.  Pellentesque condimentum,
  magna ut suscipit hendrerit, ipsum augue ornare nulla, non luctus diam neque
  sit amet urna.  Curabitur vulputate vestibulum lorem.  Fusce sagittis, libero
  non molestie mollis, magna orci ultrices dolor, at vulputate neque nulla
  lacinia eros.  Sed id ligula quis est convallis tempor.  Curabitur lacinia
  pulvinar nibh.  Nam a sapien.
- Hello World
  - Level 2
  - Another one
    - Level 3
    - more
    - more

Aliquam erat volutpat.  Nunc eleifend leo vitae magna.  In id erat non orci
commodo lobortis.  Proin neque massa, cursus ut, gravida ut, lobortis eget,
lacus.  Sed diam.  Praesent fermentum tempor tellus.  Nullam tempus.  Mauris ac
felis vel velit tristique imperdiet.  Donec at pede.  Etiam vel neque nec dui
dignissim bibendum.  Vivamus id enim.  Phasellus neque orci, porta a, aliquet
quis, semper a, massa.  Phasellus purus.  Pellentesque tristique imperdiet

1. Numbered lists
2. Second item
3. third
  1. Nesting
  2. Another

Let's try numbers with more digits
1. Single
2. Single
3. Single
4. Single
5. Single
6. Single
7. Single
8. Single
9. Single
10. Two
11. Two!

This is an html5 component with custom attributes
#+ATTR_HTML: :controls controls :width 350
#+BEGIN_video
#+HTML: <source src="movie.mp4" type="video/mp4">
#+HTML: <source src="movie.ogg" type="video/ogg">
Your browser does not support the video tag.
#+END_video

-----

#+BEGIN_aside
Lorem /ipsum/ Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Donec
hendrerit tempor tellus. Donec pretium posuere tellus. Proin quam nisl,
tincidunt et, mattis eget, convallis nec, purus. Cum sociis natoque penatibus et
magnis dis parturient montes, nascetur ridiculus mus. Nulla posuere. Donec vitae
dolor. Nullam tristique diam non turpis. Cras placerat accumsan nulla. Nullam
rutrum. Nam vestibulum accumsan nisl. Here is a [[https://github.com][link with g]]

#+BEGIN_SRC python
import test
what is this man!
#+END_SRC

[[file:../assets/favicons/mstile-310x150.png]]
#+END_aside

* Buttons
There are multiple buttons. Most common ones can be seen as tags in blog-ish
sections of this website. Here is a showcase:

@@html:<a href="#" class="btn">normal</a> @@ @@html:<a href="#" class="btn
disabled">disabled</a> @@

For inline buttons, we add ~.small~ class like this → @@html:<a href="#"
class="btn small">small</a> @@ @@html:<a href="#" class="btn small
disabled">disabled</a> @@.

Small buttons should gel well in paragraphs. Aliquam erat volutpat. Nunc
eleifend leo vitae magna. In id erat non orci commodo lobortis. Proin neque
@@html:<a href="#" class="btn small">small</a> @@ massa, cursus ut, gravida ut,
lobortis eget, lacus. Sed diam. Praesent fermentum tempor tellus.

Colored buttons are also there. Only two for now, one having the accent color
@@html:<a href="#" class="btn small primary">★ | primary</a> @@ and other with
highlight color @@html:<a href="#" class="btn small highlight">ⓘ | hear me </a>
@@. We might add more but not really. The text now is only to fill in a few
lines to cover buttons from below.

* Another title
Aliquam erat volutpat.  Nunc eleifend leo vitae magna.  In id erat non orci
commodo lobortis.  Proin neque massa, cursus ut, gravida ut, lobortis eget,
lacus.  Sed diam.  Praesent fermentum tempor tellus.  Nullam tempus.  Mauris ac
felis vel velit tristique imperdiet.  Donec at pede.  Etiam vel neque nec dui
dignissim bibendum.  Vivamus id enim.  Phasellus neque orci, porta a, aliquet
quis, semper a, massa.  Phasellus purus.  Pellentesque tristique imperdiet
tortor.  Nam euismod tellus id erat.

\[ f(x) = x + a \]

\begin{align*}
\alpha + \gamma + \sum(i) = f(x)
\end{align*}

** Hello world
Lorem ipsum dolor sit amet, consectetuer adipiscing elit.  Donec hendrerit
tempor tellus.  Donec pretium posuere tellus.  Proin quam nisl, tincidunt et,
mattis eget, convallis nec, purus.  Cum sociis natoque penatibus et magnis dis
parturient montes, nascetur ridiculus mus.  Nulla posuere.  Donec vitae dolor.
Nullam tristique diam non turpis.  Cras placerat accumsan nulla.  Nullam rutrum.
Nam vestibulum accumsan nisl.

* Property test
:PROPERTIES:
:ARCHIVE: value of archive
:END:

** TODO [#B] This is a task to be done
SCHEDULED: <2018-01-26 Fri>

Nullam eu ante vel est convallis dignissim.  Fusce suscipit, wisi nec facilisis
facilisis, est dui fermentum leo, quis tempor ligula erat quis odio.  Nunc porta
vulputate tellus.  Nunc rutrum turpis sed pede.  Sed bibendum.  Aliquam posuere.
Nunc aliquet, augue nec adipiscing interdum, lacus tellus malesuada massa, quis
varius mi purus non odio.  Pellentesque condimentum, magna ut suscipit
hendrerit, ipsum augue ornare nulla, non luctus diam neque sit amet urna.
Curabitur vulputate vestibulum lorem.  Fusce sagittis, libero non molestie
mollis, magna orci ultrices dolor, at vulputate neque nulla lacinia eros.  Sed
id ligula quis est convallis tempor.  Curabitur lacinia pulvinar nibh.  Nam a
sapien.

** DONE Something done
CLOSED: [2018-01-26 Fri 23:14]

#+BEGIN_SRC emacs-lisp
  (defun tttt ()
    (print "tttt\ntststst"))
  (tttt)
#+END_SRC

#+RESULTS:
: tttt
: tststst


Aliquam erat volutpat.  Nunc eleifend leo vitae magna.  In id erat non orci
commodo lobortis.  Proin neque massa, cursus ut, gravida ut, lobortis eget,
lacus.  Sed diam.  Praesent fermentum tempor tellus.  Nullam tempus.  Mauris ac
felis vel velit tristique imperdiet.  Donec at pede.  Etiam vel neque nec dui
dignissim bibendum.  Vivamus id enim.  Phasellus neque orci, porta a, aliquet
quis, semper a, massa.  Phasellus purus.  Pellentesque tristique imperdiet
tortor.  Nam euismod tellus id erat.

Do footnotes work? [fn::yes].

Hello world [fn:name:a definition] The Org homepage[fn:1] now looks a lot better
than it used to. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Donec
hendrerit tempor tellus. Donec pretium posuere tellus. Proin quam nisl,
tincidunt et, mattis eget, convallis nec, purus. [fn:1] Cum sociis natoque
penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nulla
posuere. Donec vitae dolor. Nullam tristique diam non turpis. Cras placerat
accumsan nulla. Nullam rutrum. Nam vestibulum accumsan nisl.

** Some other section with tags                                :hello:world:
DEADLINE: <2018-01-26 Fri>

Pellentesque dapibus suscipit ligula.  Donec posuere augue in quam.  Etiam vel
tortor sodales tellus ultricies commodo.  Suspendisse potenti.  Aenean in sem ac
leo mollis blandit.  Donec neque quam, dignissim in, mollis nec, sagittis eu,
wisi.  Phasellus lacus.  Etiam laoreet quam sed arcu.  Phasellus at dui in
ligula mollis ultricies.  Integer placerat tristique nisl.  Praesent augue.
Fusce commodo.  Vestibulum convallis, lorem a tempus semper, dui dui euismod
elit, vitae placerat urna tortor vitae lacus.  Nullam libero mauris, consequat
quis, varius et, dictum id, arcu.  Mauris mollis tincidunt felis.  Aliquam
feugiat tellus ut neque.  Nulla facilisis, risus a rhoncus fermentum, tellus
tellus lacinia purus, et dictum nunc justo sit amet elit.

** Let's add few images

#+CAPTION: Test image
[[file:../assets/favicons/mstile-310x150.png]]

Pellentesque dapibus suscipit ligula.  Donec posuere augue in quam.  Etiam vel
tortor sodales tellus ultricies commodo.  Suspendisse potenti.  Aenean in sem ac
leo mollis blandit.  Donec neque quam, dignissim in, mollis nec, sagittis eu,
wisi.  Phasellus lacus.  Etiam laoreet quam sed arcu.  Phasellus at dui in
ligula mollis ultricies.  Integer placerat tristique nisl.  Praesent augue.
Fusce commodo.  Vestibulum convallis, lorem a tempus semper, dui dui euismod
elit, vitae placerat urna tortor vitae lacus.  Nullam libero mauris, consequat
quis, varius et, dictum id, arcu.  Mauris mollis tincidunt felis.  Aliquam
feugiat tellus ut neque.  Nulla facilisis, risus a rhoncus fermentum, tellus
tellus lacinia purus, et dictum nunc justo sit amet elit.Nullam eu ante vel est
convallis dignissim.  Fusce suscipit, wisi nec facilisis facilisis, est dui
fermentum leo, quis tempor ligula erat quis odio.  Nunc porta vulputate tellus.
Nunc rutrum turpis sed pede.  Sed bibendum.  Aliquam posuere.  Nunc aliquet,
augue nec adipiscing interdum, lacus tellus malesuada massa, quis varius mi
purus non odio.  Pellentesque condimentum, magna ut suscipit hendrerit, ipsum
augue ornare nulla, non luctus diam neque sit amet urna.  Curabitur vulputate
vestibulum lorem.  Fusce sagittis, libero non molestie mollis, magna orci
ultrices dolor, at vulputate neque nulla lacinia eros.  Sed id ligula quis est
convallis tempor.  Curabitur lacinia pulvinar nibh.  Nam a sapie

For setting on click zoom, use this above the image
#+begin_src org
#+ATTR_HTML: :class zoomTarget :data-closeclick true
#+end_src

* Let's draw tables

#+CAPTION: This is the caption for the next table
|-------+-------+-------+-------|
| this  |    is |  some | table |
|-------+-------+-------+-------|
| 0.0   |    1. |     2 |     2 |
| hello | 23123 | 23131 | 23131 |
| hello | 23123 | 23131 | 23131 |
| hello | 23123 | 23131 | 23131 |
| hello | 23123 | 23131 | 23131 |
|-------+-------+-------+-------|


#+BEGIN_QUOTE
Suspendisse potenti. Donec at pede.  Sed id ligula quis est convallis tempor.
#+END_QUOTE

#+BEGIN_QUOTE
Lorem ipsum dolor sit amet, consectetuer adipiscing elit.  Donec hendrerit
tempor tellus.  Donec pretium posuere tellus.  Proin quam nisl, tincidunt et,
mattis eget, convallis nec, purus.  Cum sociis natoque penatibus et magnis dis
parturient montes, nascetur ridiculus mus.  Nulla posuere.  Donec vitae dolor.
Nullam tristique diam non turpis.  Cras placerat accumsan nulla.  Nullam rutrum.
Nam vestibulum accumsan nisl.

#+HTML:<footer>Hello</footer>
#+END_QUOTE

#+BEGIN_VERSE
This is some verse. I don't know what it is used for. I don't want to find out too.
#+END_VERSE

Citations to check if bibliographies show up cite:tukey1962future. Aliquam erat
volutpat. Nunc eleifend leo vitae magna. In id erat non orci commodo lobortis.
Proin neque massa, cursus ut, gravida ut, lobortis eget, lacus. Sed diam.
Praesent fermentum tempor tellus. Nullam tempus. Mauris ac felis vel velit
tristique imperdiet. Donec at pede. Etiam vel neque nec dui dignissim bibendum.
Vivamus id enim. Phasellus neque orci, porta a, aliquet quis, semper a, massa.
Phasellus purus. Pellentesque tristique imperdiet tortor. Nam euismod tellus id
erat.

[[bibliography:./references.bib]]

# Explicit footnotes go here
[fn:1] The link is: http://orgmode.org


#+BEGIN_edits
- Edit notes go here
- Pellentesque dapibus suscipit ligula.
#+END_edits
"""

class OrgTreeManipulationTest : StringSpec ({
    val tokens = OrgLexer(orgTreeManipulationTestText).tokenize()
    val document = parse(tokens)
})