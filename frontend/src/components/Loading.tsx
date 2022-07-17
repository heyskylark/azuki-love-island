function Loading() {
    return (
        <div className="w-full my-40 text-center">
            <div className="lds-ring"><div></div><div></div><div></div><div></div></div>
        </div>
    )
}

export function SmallLoading() {
    return (
        <div className="lds-ring-sm"><div></div><div></div><div></div><div></div></div>
    )
}

export default Loading;
