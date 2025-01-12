import asyncio
from typing import List, Optional

from fastapi import BackgroundTasks
from fastapi.routing import APIRouter

from ai_shared.databus import DataBus
from ai_shared.exceptions import NoDataSourcesToPredictError, TrainingNotFoundError
from ai_shared.persist import Persist
from ai_shared.persist.training_info import TrainingInfo
from ai_shared.ros import DataSourceRO, PostTrainBody, TrainerAiNodes
from ai_shared.vos import APIResponseVO, TrainingInfoVO
from ai_trainers import Trainer
from ai_trainers.trainers.base import TrainProcessInfo

router = APIRouter()


@router.post("/predict")
async def train_predict(body: List[DataSourceRO]):
    """
    Predict the train result, includes words, characters, tokens
    Request DataSourceRO and you will can DataSource Info Detail (except VectorDB documents)
    """
    if len(body) == 0:
        raise NoDataSourcesToPredictError("No data sources to predict")

    trainer = Trainer.new("REAL AI")

    data_sources_without_vec_docs = []
    for data_source in trainer.predict(body):
        data_sources_without_vec_docs.append(data_source.dict(exclude={"documents"}))
    return APIResponseVO.success(
        "Predict Data Sources fro ROs", data_sources_without_vec_docs
    )


@router.post("/{ai_id}/train/predict")
async def train_ai_predict(ai_id: str, body: Optional[PostTrainBody] = None):
    """
    Predict the train result, includes words, characters, tokens
    """
    trainer = Trainer.new(ai_id)
    trainer_ai_nodes = await trainer.get_trainer_ai_nodes(body)
    data_sources = trainer.load_data_sources(ai_nodes=trainer_ai_nodes.ai_nodes, is_predict=True)

    data_sources_without_document = []
    for ds in data_sources:
        data_sources_without_document.append(ds.dict())

    return APIResponseVO.success(
        f"Predict Data Sources of {ai_id}", data_sources_without_document
    )


@router.post("/{ai_id}/train")
async def train(ai_id: str, background_tasks: BackgroundTasks, body: Optional[PostTrainBody] = None):
    """\
    This api will retrieve the datasheet contents from relative tables in database by `ai_id` 
    stored in `ai_node` table. The datasheet content will be stored to vector database as 
    the *Document* object for similarity searching.
    """
    trainer = Trainer.new(ai_id)
    train_process_info: TrainProcessInfo = await trainer.new_train_process_info(body)
    background_tasks.add_task(trainer.do_train_with_process_info, train_process_info)
    # trainer.do_train_with_process_info(train_process_info)
    # https://github.com/vikadata/vikadata/issues/8972
    await asyncio.sleep(2)
    return APIResponseVO.success(
        f"AI_ID: {ai_id}, training started asynchronously. Please check status by get AI infos",
        train_process_info.dict(),
    )


@router.get("/{ai_id}/trainings/{training_id}")
async def training_info(ai_id: str, training_id: str):
    """
    Training ID is the name of Vector DB persist name
    """
    training = await TrainingInfo.load_training_info(ai_id, training_id)
    if not training:
        raise TrainingNotFoundError(f"ai_id: {ai_id}, training_id: {training_id}")

    training_info_vo = TrainingInfoVO(**training.dict())
    # exist folder?
    # is it being locking?
    return APIResponseVO.success(
        msg=f"AI_ID: {ai_id}, training info of Training ID: {training_id}",
        data=training_info_vo.dict(),
    )
