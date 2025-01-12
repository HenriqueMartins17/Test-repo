import pathlib
from datetime import datetime

import jieba
import pydantic
import pytest

from ai_shared.config import settings
from ai_shared.exceptions import AiNotFoundError, TrainingNotFoundError
from ai_shared.persist import Persist
from ai_shared.persist.ai_info import AIInfo
from ai_shared.persist.data_source import count_words_and_characters
from ai_shared.persist.training_info import TrainingInfo, TrainingStatus
from ai_trainers.trainers.base import TrainProcessInfo


class TestPersist:
    @pytest.mark.asyncio
    async def test_words_chars_count(self):
        the_str = "乒乓球拍卖完了"
        words = jieba.cut(the_str)
        assert len(list(words)) == 4

        words, chars = count_words_and_characters(the_str)
        assert words == 4
        assert chars == 7

        the_str = "This is a test"
        words = jieba.cut(the_str)
        count = 0
        for c in words:
            if c.strip() != "":
                count += 1
        assert count == 4
        words, chars = count_words_and_characters(the_str)
        assert words == 4
        assert chars == 14

    @pytest.mark.asyncio
    async def test_not_exist(self):
        ai_id = "not_exist"
        training_id = "not_exist"
        ai_info = await AIInfo.load_ai_info(ai_id)
        assert ai_info is None

        validation_error_raised = False
        try:
            ai_info = AIInfo()
            await ai_info.save()
        except pydantic.error_wrappers.ValidationError:
            validation_error_raised = True
        assert validation_error_raised

        training_info = await TrainingInfo.load_training_info(ai_id, training_id)
        assert training_info is None

        training_file_validation_error_raised = False
        try:
            training_info = TrainingInfo()
            await training_info.save()
        except pydantic.error_wrappers.ValidationError:
            training_file_validation_error_raised = True
        assert training_file_validation_error_raised

    @pytest.mark.asyncio
    async def test_load_ai_info(self):
        ai_id = "mock_load"
        ai_model = AIInfo(ai_id=ai_id, current_training_id="test")
        await ai_model.save()

        result = await AIInfo.load_ai_info(ai_id)
        assert result.ai_id == ai_id
        assert result.current_training_id == "test"
        assert result.locking_training_id is None
        assert result.success_train_history == []

    @pytest.mark.asyncio
    async def test_save_ai_info(self):
        ai_id = "mock_load"

        ai_info = await AIInfo.load_ai_info(ai_id)
        ai_info.current_training_id = "test_save_ai_info"
        await ai_info.save()

        ai_info2 = await AIInfo.load_ai_info(ai_id)
        assert ai_info2.current_training_id == "test_save_ai_info"

    @pytest.mark.asyncio
    async def test_make_training_id(self):
        ai_id = "test"

        # Call the `make_training_id` method
        result = TrainProcessInfo.make_training_id(ai_id)

        # Assert that the result is a string
        assert isinstance(result, str)
        timestamp = result.split("_")
        assert datetime.strptime(timestamp[1], "%Y%m%d%H%M%S")

    @pytest.mark.asyncio
    async def test_ensure_path_exists(self):
        file_path = "./.data/test.json"
        Persist.ensure_directory_exists(file_path)
        assert pathlib.Path(file_path).parent.exists()

    @pytest.mark.asyncio
    async def test_get_root_path(self):
        # Call the `get_root_path` method
        result = Persist.get_root_path()

        # Assert that the result is a string
        assert isinstance(result, str)
        assert result == str(settings.persistent_data_root)


@pytest.mark.asyncio
async def io_lock_test_fn():
    t = await TrainingInfo.load_training_info("io_lock_test", "io_lock_test")
    await t.save()
    ai_info = await AIInfo.load_ai_info("io_lock_test")
    assert "io_lock_test" in ai_info.success_train_history
    await ai_info.save()


class TestAIInfo:
    mock_test_ai_id = "mocktest"

    @pytest.mark.asyncio
    async def test_new_ai(self):
        await AIInfo.new(self.mock_test_ai_id)

    @pytest.mark.asyncio
    async def test_io_lock(self):
        from multiprocessing import Process

        ps = []
        for _i in range(10):
            p = Process(target=io_lock_test_fn)
            ps.append(p)
            p.start()

        for p in ps:
            p.join()

    @pytest.mark.asyncio
    async def test_get_training_path(self):
        ai_model = await AIInfo.load_ai_info(self.mock_test_ai_id)
        training_id = "456"

        await TrainingInfo(
            ai_id=ai_model.ai_id, training_id=training_id
        ).save()  # new training info

        training_info = await ai_model.get_training_info(training_id)
        assert training_info.ai_id == ai_model.ai_id
        assert training_info.status == TrainingStatus.NEW

        path = training_info.get_training_path()
        assert f"{Persist.get_root_path()}/mocktest/{training_id}" == path

        training_folders = ai_model.get_trainings_folders()
        assert "456" in training_folders

    @pytest.mark.asyncio
    async def test_save(self):
        ai_model = await AIInfo.load_ai_info(self.mock_test_ai_id)
        await ai_model.save()
        file_path = AIInfo.get_ai_info_file_path(ai_model.ai_id)
        assert pathlib.Path(file_path).exists()

    @pytest.mark.asyncio
    async def test_get_conversation_histories(self):
        ai_model = await AIInfo.load_ai_info(self.mock_test_ai_id)
        conversation_id = "abc"
        root = Persist.get_root_path()
        training_id = f"{ai_model.ai_id}_test"
        ai_model.current_training_id = training_id
        await ai_model.save()

        dpath = pathlib.Path(root).joinpath(ai_model.ai_id, training_id, "conversations")
        dpath.mkdir(exist_ok=True, parents=True)

        fpath = dpath / f"{conversation_id}.json"
        fpath.write_text('[{"message": "test"}]', encoding="utf-8")

        if TrainingInfo.exist(ai_model.ai_id, ai_model.current_training_id):
            current_training = await ai_model.get_training_info(ai_model.current_training_id)
            result = await current_training.get_conversation(conversation_id)

            assert len(result) == 1
            assert result[0]["message"] == "test"

        fpath.unlink()
        ai_model.current_training_id = None

    @pytest.mark.asyncio
    async def test_get_conversation_histories_file_path(self):
        ai_model = await AIInfo.load_ai_info(self.mock_test_ai_id)
        conversation_id = "abc"
        root = Persist.get_root_path()
        training_id = f"{ai_model.ai_id}_test"
        ai_model.current_training_id = training_id
        await ai_model.save()

        dpath = pathlib.Path(root).joinpath(ai_model.ai_id, training_id, "conversations")
        dpath.mkdir(exist_ok=True, parents=True)

        fpath = dpath / f"{conversation_id}.json"

        result = TrainingInfo.make_conversation_file_path(
            ai_model.ai_id, ai_model.current_training_id, conversation_id
        )
        assert result == str(fpath)

        ai_model.current_training_id = None

    @pytest.mark.asyncio
    async def test_lock(self):
        ai_model = await AIInfo.load_ai_info(self.mock_test_ai_id)
        training_id = "456"
        await ai_model.lock(training_id)
        ai_info = await AIInfo.load_ai_info(ai_model.ai_id)
        assert ai_info.locking_training_id == training_id

    @pytest.mark.asyncio
    async def test_unlock(self):
        ai_model = await AIInfo.load_ai_info(self.mock_test_ai_id)
        await ai_model.unlock()
        ai_info = await AIInfo.load_ai_info(ai_model.ai_id)
        assert ai_info.locking_training_id is None
