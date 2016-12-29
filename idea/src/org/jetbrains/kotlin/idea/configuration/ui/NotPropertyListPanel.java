/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.configuration.ui;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.ui.AddEditRemovePanel;
import org.jetbrains.kotlin.name.FqNameUnsafe;

import java.util.List;

public class NotPropertyListPanel extends AddEditRemovePanel<FqNameUnsafe> {

    public boolean modified = false;

    public NotPropertyListPanel(List<FqNameUnsafe> data) {
        super(new MyTableModel(), data, "Excluded");
    }

    @Override
    public boolean removeItem(FqNameUnsafe fqName) {
        return true;
    }

    @Override
    public FqNameUnsafe editItem(FqNameUnsafe o) {

        String result = Messages.showInputDialog(this, "Enter Full Qualified Name",
                                                 "Edit Exclusion",
                                                 Messages.getQuestionIcon(),
                                                 o.asString(),
                                                 new NonEmptyInputValidator()
        );
        if (result == null) {
            return null;
        }

        FqNameUnsafe created = new FqNameUnsafe(result);
        if (getData().contains(created)) {
            return null;
        }
        modified = true;
        return created;
    }

    @Override
    public FqNameUnsafe addItem() {
        String result = Messages.showInputDialog(this, "Enter Full Qualified Name",
                                                 "Add Exclusion",
                                                 Messages.getQuestionIcon(),
                                                 "",
                                                 new NonEmptyInputValidator()
        );
        if (result == null) {
            return null;
        }

        FqNameUnsafe created = new FqNameUnsafe(result);
        if (getData().contains(created)) {
            return null;
        }
        modified = true;
        return created;
    }

    private static class MyTableModel extends TableModel<FqNameUnsafe> {
        @Override
        public Object getField(FqNameUnsafe o, int columnIndex) {
            return o.asString();
        }

        @Override
        public String getColumnName(int columnIndex) {
            return "Full qualified name";
        }

        @Override
        public int getColumnCount() {
            return 1;
        }
    }
}

