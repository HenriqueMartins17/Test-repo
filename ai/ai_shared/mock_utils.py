def is_mock(_id: str | int) -> bool:
    # mock ai id
    if str(_id) in (
            "mock",
            "not_exist",
            "mock_core",
            "mock_load",
            "mocktest",
            "copilot",
    ):
        return True
    if "mock" in str(_id):
        return True

    # mock ai node id
    if str(_id) in (
        "100",
        "101",
    ):
        return True

    return False
