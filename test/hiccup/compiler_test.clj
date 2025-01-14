(ns hiccup.compiler-test
  (:require [clojure.test :refer :all]
            [hiccup2.core :refer [html]]))

(deftest test-compile-element-literal-tag
  ;; `compile-element ::literal-tag` behavior varies based on the following
  ;; things, so we need to test all their combinations:
  ;; - mode: xhtml, html, xml, sgml
  ;; - runtime type of the first child: attributes, content, nil
  ;; - tag: normal element, void element

  (testing "runtime attributes,"
    (testing "normal tag"
      (is (= (str (html {:mode :xhtml} [:p (identity {:id 1})]))
             "<p id=\"1\"></p>"))
      (is (= (str (html {:mode :html} [:p (identity {:id 1})]))
             "<p id=\"1\"></p>"))
      (is (= (str (html {:mode :xml} [:p (identity {:id 1})]))
             "<p id=\"1\" />"))
      (is (= (str (html {:mode :sgml} [:p (identity {:id 1})]))
             "<p id=\"1\">")))
    (testing "void tag"
      (is (= (str (html {:mode :xhtml} [:br (identity {:id 1})]))
             "<br id=\"1\" />"))
      (is (= (str (html {:mode :html} [:br (identity {:id 1})]))
             "<br id=\"1\">"))
      (is (= (str (html {:mode :xml} [:br (identity {:id 1})]))
             "<br id=\"1\" />"))
      (is (= (str (html {:mode :sgml} [:br (identity {:id 1})]))
             "<br id=\"1\">"))))

  (testing "runtime content,"
    (testing "normal tag"
      (is (= (str (html {:mode :xhtml} [:p (identity "x")])) "<p>x</p>"))
      (is (= (str (html {:mode :html} [:p (identity "x")])) "<p>x</p>"))
      (is (= (str (html {:mode :xml} [:p (identity "x")])) "<p>x</p>"))
      (is (= (str (html {:mode :sgml} [:p (identity "x")])) "<p>x</p>")))
    (testing "void tag"
      ;; it's not valid HTML to have content inside void elements,
      ;; but Hiccup should still obey what the user told it to do
      (is (= (str (html {:mode :xhtml} [:br (identity "x")])) "<br>x</br>"))
      (is (= (str (html {:mode :html} [:br (identity "x")])) "<br>x</br>"))
      (is (= (str (html {:mode :xml} [:br (identity "x")])) "<br>x</br>"))
      (is (= (str (html {:mode :sgml} [:br (identity "x")])) "<br>x</br>"))))

  (testing "runtime nil,"
    (testing "normal tag"
      (is (= (str (html {:mode :xhtml} [:p (identity nil)])) "<p></p>"))
      (is (= (str (html {:mode :html} [:p (identity nil)])) "<p></p>"))
      (is (= (str (html {:mode :xml} [:p (identity nil)])) "<p></p>"))
      (is (= (str (html {:mode :sgml} [:p (identity nil)])) "<p></p>")))
    (testing "void tag"
      ;; TODO: this might not be desired behavior (use case: the user has
      ;;       a function which returns a map of attributes or nil)
      (is (= (str (html {:mode :xhtml} [:br (identity nil)])) "<br></br>"))
      (is (= (str (html {:mode :html} [:br (identity nil)])) "<br></br>"))
      (is (= (str (html {:mode :xml} [:br (identity nil)])) "<br></br>"))
      (is (= (str (html {:mode :sgml} [:br (identity nil)])) "<br></br>")))))

(deftest test-compile-element-default
  (testing "runtime tag"
    (is (= (str (html {:mode :xhtml} [(identity :p)])) "<p></p>"))
    (is (= (str (html {:mode :html} [(identity :p)])) "<p></p>"))
    (is (= (str (html {:mode :xml} [(identity :p)])) "<p />"))
    (is (= (str (html {:mode :sgml} [(identity :p)])) "<p>")))

  (testing "runtime tag with attributes"
    (is (= (str (html {:mode :xhtml} [(identity :p) {:id 1}]))
           (str (html {:mode :xhtml} [(identity :p) (identity {:id 1})]))
           "<p id=\"1\"></p>"))
    (is (= (str (html {:mode :html} [(identity :p) {:id 1}]))
           (str (html {:mode :html} [(identity :p) (identity {:id 1})]))
           "<p id=\"1\"></p>"))
    (is (= (str (html {:mode :xml} [(identity :p) {:id 1}]))
           (str (html {:mode :xml} [(identity :p) (identity {:id 1})]))
           "<p id=\"1\" />"))
    (is (= (str (html {:mode :sgml} [(identity :p) {:id 1}]))
           (str (html {:mode :sgml} [(identity :p) (identity {:id 1})]))
           "<p id=\"1\">")))

  (testing "runtime tag with text content"
    (is (= (str (html {:mode :xhtml} [(identity :p) "x"]))
           (str (html {:mode :xhtml} [(identity :p) (identity "x")]))
           "<p>x</p>"))
    (is (= (str (html {:mode :html} [(identity :p) "x"]))
           (str (html {:mode :html} [(identity :p) (identity "x")]))
           "<p>x</p>"))
    (is (= (str (html {:mode :xml} [(identity :p) "x"]))
           (str (html {:mode :xml} [(identity :p) (identity "x")]))
           "<p>x</p>"))
    (is (= (str (html {:mode :sgml} [(identity :p) "x"]))
           (str (html {:mode :sgml} [(identity :p) (identity "x")]))
           "<p>x</p>")))

  (testing "runtime tag with child elements"
    ;; FIXME: this should return "<p><span>x</span></p>"
    (is (= (str (html {:mode :xhtml} [(identity :p) [:span "x"]]))
           "<p>&lt;span&gt;x&lt;/span&gt;</p>"))
    (is (= (str (html {:mode :html} [(identity :p) [:span "x"]]))
           "<p>&lt;span&gt;x&lt;/span&gt;</p>"))
    (is (= (str (html {:mode :xml} [(identity :p) [:span "x"]]))
           "<p>&lt;span&gt;x&lt;/span&gt;</p>"))
    (is (= (str (html {:mode :sgml} [(identity :p) [:span "x"]]))
           "<p>&lt;span&gt;x&lt;/span&gt;</p>"))

    (is (= (str (html {:mode :xhtml} [(identity :p) (identity [:span "x"])]))
           "<p><span>x</span></p>"))
    (is (= (str (html {:mode :html} [(identity :p) (identity [:span "x"])]))
           "<p><span>x</span></p>"))
    (is (= (str (html {:mode :xml} [(identity :p) (identity [:span "x"])]))
           "<p><span>x</span></p>"))
    (is (= (str (html {:mode :sgml} [(identity :p) (identity [:span "x"])]))
           "<p><span>x</span></p>"))))
