/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.dmn.core.ast;

import java.util.Collection;
import java.util.List;

import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.event.DMNRuntimeEventManager;
import org.kie.dmn.core.api.DMNExpressionEvaluator;
import org.kie.dmn.core.api.EvaluatorResult;
import org.kie.dmn.core.api.EvaluatorResult.ResultType;
import org.kie.dmn.core.compiler.DMNProfile;
import org.kie.dmn.core.impl.DMNRuntimeImpl;
import org.kie.dmn.feel.FEEL;
import org.kie.dmn.feel.codegen.feel11.ProcessedExpression;
import org.kie.dmn.feel.lang.CompiledExpression;
import org.kie.dmn.feel.lang.impl.CompiledExpressionImpl;
import org.kie.dmn.feel.lang.impl.EvaluationContextImpl;
import org.kie.dmn.feel.lang.impl.FEELImpl;

/**
 * An evaluator for DMN Literal Expressions
 */
public class DMNLiteralExpressionEvaluator
        implements DMNExpressionEvaluator {
    private CompiledExpression expression;
    private boolean isFunctionDef;

    public DMNLiteralExpressionEvaluator(CompiledExpression expression) {
        this.expression = expression;
        if (expression instanceof CompiledExpressionImpl) {
            this.isFunctionDef = ((CompiledExpressionImpl) expression).isFunctionDef();
        } else if (expression instanceof ProcessedExpression) {
            this.isFunctionDef = ((ProcessedExpression) expression).getInterpreted().isFunctionDef();
        } else {
            throw new IllegalArgumentException(
                    "Cannot create DMNLiteralExpressionEvaluator: unsupported type " + expression.getClass());
        }
    }

    public boolean isFunctionDefinition() {
        return isFunctionDef;
    }

    public CompiledExpression getExpression() {
        return this.expression;
    }

    @Override
    public EvaluatorResult evaluate(DMNRuntimeEventManager dmrem, DMNResult result) {
        // in case an exception is thrown, the parent node will report it
        List<DMNProfile> profiles = ((DMNRuntimeImpl) dmrem.getRuntime()).getProfiles();
        @SuppressWarnings({"unchecked", "rawtypes"})
        FEELImpl feelInstance = (FEELImpl) FEEL.newInstance(dmrem.getRuntime().getRootClassLoader(), (List) profiles);
        @SuppressWarnings({"unchecked", "rawtypes"})
        EvaluationContextImpl ectx = feelInstance.newEvaluationContext((Collection) dmrem.getListeners(), result.getContext().getAll());
        ectx.setDMNRuntime(dmrem.getRuntime());
        Object val = feelInstance.evaluate(expression, ectx);
        return new EvaluatorResultImpl( val, ResultType.SUCCESS );
    }
}
