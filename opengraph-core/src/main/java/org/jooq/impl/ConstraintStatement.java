package org.jooq.impl;


import org.jooq.Constraint;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Table;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * jooq accessable wrapper for parsing DDL queries fragments
 */
public class ConstraintStatement implements Constraint {
    private Field<?>[] foreignKey;
    private Field<?>[] primaryKey;
    private ConstraintImpl constraintImpl;
    private Table<?> $referencesTable;
    private Field<?>[] references;

    public ConstraintStatement(Constraint constraint) {
        assert ConstraintImpl.class.isAssignableFrom(constraint.getClass());
        constraintImpl = (ConstraintImpl) constraint;
        foreignKey = constraintImpl.$foreignKey();
        primaryKey = constraintImpl.$primaryKey();
        $referencesTable = constraintImpl.$referencesTable();
        references = constraintImpl.$references();
    }

    public Field<?>[] getReferences() {
        return Objects.isNull(references) ? new Field[]{} : references;
    }

    public Table<?> get$referencesTable() {
        return $referencesTable;
    }

    public Field<?>[] getForeignKey() {
        return Objects.isNull(foreignKey) ? new Field[]{} : foreignKey;
    }

    public Field<?>[] getPrimaryKey() {
        return Objects.isNull(primaryKey) ? new Field[]{} : primaryKey;
    }

    @Override
    public String getName() {
        return constraintImpl.getName();
    }

    @Override
    public Name getQualifiedName() {
        return constraintImpl.getQualifiedName();
    }

    @Override
    public Name getUnqualifiedName() {
        return constraintImpl.getUnqualifiedName();
    }

    @Override
    public String getComment() {
        return constraintImpl.getComment();
    }


    public static List<ConstraintStatement> foreignKey(List<ConstraintStatement> constraints) {
        return constraints.stream().noneMatch(c -> c.getForeignKey().length > 0) ?
                Collections.emptyList() : constraints.stream().filter(c -> c.getForeignKey().length > 0).collect(Collectors.toList());
    }

    public static List<ConstraintStatement> primaryKey(List<ConstraintStatement> constraints) {
        return constraints.stream().noneMatch(c -> c.getPrimaryKey().length > 0) ?
                Collections.emptyList() : constraints.stream().filter(c -> c.getPrimaryKey().length > 0).collect(Collectors.toList());
    }
}
