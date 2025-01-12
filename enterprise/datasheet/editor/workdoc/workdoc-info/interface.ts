export interface IWorkdocInfo {
    documentId: string;
}

export interface IWorkdocInfoResponse {
    createdAt: number;
    creatorAvatar: string;
    creatorUuid: string;
    creatorName: string;
    lastModifiedAt: number;
    lastModifiedAvatar: string;
    lastModifiedBy: string;
    lastModifiedByUuid: string;
}
