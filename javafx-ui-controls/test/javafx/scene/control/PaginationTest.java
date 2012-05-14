/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
 */

package javafx.scene.control;

import com.sun.javafx.css.StyleableProperty;
import com.sun.javafx.pgstub.StubToolkit;
import static javafx.scene.control.ControlTestUtils.*;
import com.sun.javafx.scene.control.Pagination;
import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PaginationTest {
    private Pagination pagination;
    private Toolkit tk;
    private Scene scene;
    private Stage stage;
    private StackPane root;

    @Before public void setup() {
        pagination = new Pagination();
        tk = (StubToolkit)Toolkit.getToolkit();//This step is not needed (Just to make sure StubToolkit is loaded into VM)
        root = new StackPane();
        scene = new Scene(root);
        stage = new Stage();
        stage.setScene(scene);
    }

    /*********************************************************************
     * Helper methods                                                    *
     ********************************************************************/
    private void show() {
        stage.show();
    }

    /*********************************************************************
     * Tests for default values                                         *
     ********************************************************************/

    @Test public void defaultConstructorShouldSetStyleClassTo_pagination() {
        assertStyleClassContains(pagination, "pagination");
    }

    @Test public void defaultCurrentPageIndex() {
        assertEquals(pagination.getCurrentPageIndex(), 0);
    }

    @Test public void defaultPageCount() {
        assertEquals(pagination.getPageCount(), Pagination.INDETERMINATE);
    }

    @Test public void defaultPageFactory() {
        assertNull(pagination.getPageFactory());
    }

    @Test public void defaultMaxPageIndicatorCount() {
        assertEquals(pagination.getMaxPageIndicatorCount(), 10);
    }

    /*********************************************************************
     * Tests for property binding                                        *
     ********************************************************************/

    @Test public void checkMaxPageIndicatorCountPropertyBind() {
        IntegerProperty intPr = new SimpleIntegerProperty(200);
        pagination.maxPageIndicatorCountProperty().bind(intPr);
        assertEquals("number of visible pages cannot be bound", pagination.maxPageIndicatorCountProperty().getValue(), 200, 0);
        intPr.setValue(105);
        assertEquals("number of visible pages cannot be bound", pagination.maxPageIndicatorCountProperty().getValue(), 105, 0);
    }

    @Test public void checkPageIndexPropertyBind() {
        IntegerProperty intPr = new SimpleIntegerProperty(10);
        pagination.currentPageIndexProperty().bind(intPr);
        assertEquals("page index cannot be bound", pagination.currentPageIndexProperty().getValue(), 10, 0);
        intPr.setValue(20);
        assertEquals("page index cannot be bound", pagination.currentPageIndexProperty().getValue(), 20, 0);
    }

    @Test public void checkPageFactoryPropertyBind() {
        Callback callback = new Callback() {
            @Override
            public Object call(Object arg0) {
                return null;
            }
        };
        ObjectProperty objPr = new SimpleObjectProperty(callback);
        pagination.pageFactoryProperty().bind(objPr);
        assertSame("page factory cannot be bound", pagination.pageFactoryProperty().getValue(), callback);
    }

    /*********************************************************************
     * CSS related Tests                                                 *
     ********************************************************************/
    @Test public void whenMaxPageIndicatorCountIsBound_impl_cssSettable_ReturnsFalse() {
        StyleableProperty styleable = StyleableProperty.getStyleableProperty(pagination.maxPageIndicatorCountProperty());
        assertTrue(styleable.isSettable(pagination));
        IntegerProperty intPr = new SimpleIntegerProperty(10);
        pagination.maxPageIndicatorCountProperty().bind(intPr);
        assertFalse(styleable.isSettable(pagination));
    }

    @Test public void whenMaxPageIndicatorCountIsSpecifiedViaCSSAndIsNotBound_impl_cssSettable_ReturnsTrue() {
        StyleableProperty styleable = StyleableProperty.getStyleableProperty(pagination.maxPageIndicatorCountProperty());
        styleable.set(pagination, 100);
        assertTrue(styleable.isSettable(pagination));
    }

    @Test public void canSpecifyMaxPageIndicatorCountViaCSS() {
        StyleableProperty styleable = StyleableProperty.getStyleableProperty(pagination.maxPageIndicatorCountProperty());
        styleable.set(pagination, 100);
        assertSame(100, pagination.getMaxPageIndicatorCount());
    }

    /********************************************************************
     * Miscellaneous Tests                                              *
     ********************************************************************/

    @Test public void setCurrentPageIndexAndNavigateWithKeyBoard() {
        pagination.setPageCount(25);
        pagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer pageIndex) {
                Node n = createPage(pageIndex);
                return n;
            }
        });
        root.setPrefSize(400, 400);
        root.getChildren().add(pagination);
        show();

        tk.firePulse();
        assertTrue(pagination.isFocused());

        KeyEventFirer keyboard = new KeyEventFirer(pagination);
        keyboard.doRightArrowPress();
        tk.firePulse();

        assertEquals(1, pagination.getCurrentPageIndex());

        keyboard.doRightArrowPress();
        tk.firePulse();

        assertEquals(2, pagination.getCurrentPageIndex());
    }

    @Ignore @Test public void setCurrentPageIndexAndNavigateWithMouse() {
        pagination.setPageCount(25);
        pagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer pageIndex) {
                Node n = createPage(pageIndex);
                return n;
            }
        });

        root.setPrefSize(400, 400);
        root.getChildren().add(pagination);
        show();

        root.impl_reapplyCSS();
        root.layout();
        tk.firePulse();
        assertTrue(pagination.isFocused());

        double xval = (pagination.localToScene(pagination.getLayoutBounds())).getMinX();
        double yval = (pagination.localToScene(pagination.getLayoutBounds())).getMinY();

        scene.impl_processMouseEvent(
            MouseEventGenerator.generateMouseEvent(MouseEvent.MOUSE_PRESSED, xval+170, yval+380));
        tk.firePulse();

        assertEquals(3, pagination.getCurrentPageIndex());
    }

    @Test public void setCurrentPageIndexAndVerifyCallback() {
        pagination.setPageCount(25);
        pagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer pageIndex) {
                Node n = createPage(pageIndex);
                assertTrue(pageIndex == 0 || pageIndex == 4);
                return n;
            }
        });

        root.setPrefSize(400, 400);
        root.getChildren().add(pagination);
        show();

        pagination.setCurrentPageIndex(4);
    }

    @Test public void setMaxPageIndicatorCountToZero() {
        pagination.setPageCount(0);

        root.setPrefSize(400, 400);
        root.getChildren().add(pagination);
        show();

        assertEquals(Integer.MAX_VALUE, pagination.getPageCount());
    }

    @Test public void setCurrentPageIndexLessThanZero() {
        pagination.setPageCount(100);
        root.setPrefSize(400, 400);
        root.getChildren().add(pagination);
        show();

        pagination.setCurrentPageIndex(5);
        pagination.setCurrentPageIndex(-1);
        assertEquals(5, pagination.getCurrentPageIndex());
    }

    public VBox createPage(int pageIndex) {
        VBox box = new VBox(5);
        int page = pageIndex * 10;
        for (int i = page; i < page + 10; i++) {
            Label l = new Label("PAGE INDEX " + pageIndex);
            box.getChildren().add(l);
        }
        return box;
    }
}