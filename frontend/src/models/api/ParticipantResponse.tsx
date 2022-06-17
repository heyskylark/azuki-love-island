interface ParticipantResponse {
    id: string;
    azukiId: number;
    ownerAddress: string;
    imageUrl: string;
    backgroundTrait: string;
    twitterHandle: string;
    seasonNumber: number;
    gender: string;
    bio: string | undefined;
    hobbies: string[] | undefined;
    submitted: boolean;
    validated: boolean;
}

export default ParticipantResponse;
