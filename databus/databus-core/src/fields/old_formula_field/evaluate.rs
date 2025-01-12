use std::collections::HashMap;

pub struct ExpCache {
    cache: HashMap<String, Result<IFormulaExpr, IFormulaError>>,
}

impl ExpCache {
    pub fn new() -> Self {
        ExpCache {
            cache: HashMap::new(),
        }
    }

    pub fn set(&mut self, datasheet_id: &str, field_id: &str, exp: &str, ast: Result<IFormulaExpr, IFormulaError>) {
        self.cache.insert(format!("{}{}{}", datasheet_id, field_id, exp), ast);
    }

    pub fn get(&self, datasheet_id: &str, field_id: &str, exp: &str) -> Option<&Result<IFormulaExpr, IFormulaError>> {
        self.cache.get(&format!("{}{}{}", datasheet_id, field_id, exp))
    }

    pub fn has(&self, datasheet_id: &str, field_id: &str, exp: &str) -> bool {
        self.cache.contains_key(&format!("{}{}{}", datasheet_id, field_id, exp))
    }

    pub fn del(&mut self, datasheet_id: &str, field_id: &str, exp: &str) -> Option<Result<IFormulaExpr, IFormulaError>> {
        self.cache.remove(&format!("{}{}{}", datasheet_id, field_id, exp))
    }

    pub fn clear_all(&mut self) {
        self.cache.clear();
    }
}

pub fn evaluate(expression: &str, context: &FormulaEvaluateContext, should_throw: bool, force_throw: bool
) -> anyhow::Result<Option<CellValueVo>> {
    if expression.is_empty() {
        return Ok(None);
    }
    let FormulaEvaluateContext { snapshot, field, record } = context;
    // let state = &context.state;
    // let field_map = getFieldMap(state, context.field.property.datasheetId).unwrap();
    let field_map = &snapshot.meta.field_map;
    let f_expr = parse(expression, &FormulaContext { field: context.field, field_map, snapshot });
    if let Err(f_expr_error) = f_expr {
        if force_throw {
            return Err(Box::new(f_expr_error));
        }
        return Ok(None);
    }
    
    let resolver_fn = resolver_wrapper(context);
    let interpreter = Interpreter::new(resolver_fn, context);
    let result = interpreter.visit(f_expr.ast)?;
    
    // Error for NaN/Infinite/-Infinite values
    if result.is_finite() {
        return Ok(result);
    } else {
        return Err(Box::new(FormulaBaseError::new("NaN")));
    }
}

pub fn parse(
    expression: &str,
    context: &FormulaContext,
    update_cache: bool,
) -> anyhow::Result<FormulaExpr> {
    if expression.trim().is_empty() {
        // return Err(FormulaError::new(Strings::function_content_empty()));
        return Err(anyhow::anyhow!("function_content_empty"));
    }

    let field = &context.field;
    let datasheet_id = field
        .property
        .as_ref()
        .and_then(|property| property.datasheet_id.clone())
        .unwrap_or_default();

    if !update_cache && ExpCache::has(&datasheet_id, &field.id, expression) {
        return Ok(ExpCache::get(&datasheet_id, &field.id, expression).unwrap());
    }

    let lexer = FormulaExprLexer::new(expression);
    if !lexer.errors.is_empty() {
        ExpCache::set(
            &datasheet_id,
            &field.id,
            expression,
            Err(lexer.errors[0].clone()),
        );
    } else {
        let ast = FormulaExprParser::new(lexer, context).parse();
        ExpCache::set(&datasheet_id, &field.id, expression, Ok(ast));
    }

    Ok(ExpCache::get(&datasheet_id, &field.id, expression).unwrap())
}