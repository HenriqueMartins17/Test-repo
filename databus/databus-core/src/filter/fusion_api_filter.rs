use std::rc::Rc;
// use databus_shared::env_var;
use crate::so::{ViewSO, DatasheetPackContext, ViewColumnSO};


// const ROW_FILTER_OFFLOAD_COMPLEXITY_THRESHOLD_ENV: &str = "ROW_FILTER_OFFLOAD_COMPLEXITY_THRESHOLD";
// const MAX_WORKERS_ENV: &str = "MAX_WORKERS";

// // fn get_row_filter_offload_complexity_threshold() -> f64 {
// //     match env_var!(ROW_FILTER_OFFLOAD_COMPLEXITY_THRESHOLD_ENV) {
//         // Ok(val) => val.parse::<f64>().unwrap_or(f64::INFINITY),
//         // Err(_) => f64::INFINITY,
// //     }
// // }

// fn measure_visible_row_filtering_complexity(rows: &Vec<ViewRowSO>, node: Option<AstNode>) -> usize {
//     match node {
//         Some(node) => node.num_nodes * rows.len(),
//         None => rows.len(),
//     }
// }

// fn get_max_workers() -> Option<NonZeroUsize> {
//     match env::var(MAX_WORKERS_ENV) {
//         Ok(val) => NonZeroUsize::new(val.parse::<usize>().unwrap_or(4)),
//         Err(_) => NonZeroUsize::new((1.5 * num_cpus::get() as f64).round() as usize),
//     }
// }

pub struct FusionApiFilter {
    // field_name: &'static str,
    // piscina: Option<Piscina>,
    // logger: Logger,
}

impl FusionApiFilter {
    pub fn new(
        // logger: Logger
    ) -> Self {
        // let ROW_FILTER_OFFLOAD_COMPLEXITY_THRESHOLD: f64 = get_row_filter_offload_complexity_threshold();
        // let MAX_WORKERS: Option<NonZeroUsize> = get_max_workers();

        // let max_workers = env::var("MAX_WORKERS").unwrap_or_default().parse::<usize>().unwrap_or(0);
        // let row_filter_offload_complexity_threshold = env::var("ROW_FILTER_OFFLOAD_COMPLEXITY_THRESHOLD").unwrap_or_default().parse::<f64>().unwrap_or(f64::INFINITY);
        // let piscina = if max_workers > 0 && row_filter_offload_complexity_threshold.is_finite() {
        //     let filename = Path::new(env!("CARGO_MANIFEST_DIR"))
        //         .join("src")
        //         .join("fusion_api_filter_worker.rs")
        //         .to_str()
        //         .unwrap()
        //         .to_string();
        //     Some(Piscina::new(NonZeroUsize::new(max_workers).unwrap(), filename))
        // } else {
        //     None
        // };

        FusionApiFilter {
            // field_name: "Virtual",
            // piscina,
            // logger,
        }
    }

    pub fn get_columns_by_view_id(
        &self, 
        context: Rc<DatasheetPackContext>,
        _dst_id: &str, 
        view: Option<&ViewSO>
    ) -> Vec<ViewColumnSO> {
        // let datasheet = context.get_datasheet(dst_id).unwrap();
        let field_map = context.datasheet_pack.snapshot.meta.field_map.clone();
    
        match view {
            Some(view) => {
                view.columns.iter().filter_map(|cur| {
                    let field = field_map.get(&cur.field_id);
                    match (field, cur.hidden.unwrap_or(false)) {
                        (Some(_), false) => Some(cur.clone()),
                        _ => None,
                    }
                }).collect()
            },
            None => {
                field_map.keys().map(|field_id| ViewColumnSO { field_id: field_id.clone(), ..Default::default() }).collect()
            }
        }
    }
}


