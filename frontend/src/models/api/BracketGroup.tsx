export interface BracketGroupCreator {
    submissionId1?: string;
    submissionId2?: string;
}

interface BracketGroup {
    submissionId1: string;
    submissionId2?: string;
    sortOrder: number;
}

export default BracketGroup;
